package pliska.communicationgraphclusteringbackend.db.graph;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pliska.communicationgraphclusteringbackend.clustering.LlmService;
import pliska.communicationgraphclusteringbackend.db.email.EmailEntity;
import pliska.communicationgraphclusteringbackend.db.email.EmailRepository;
import pliska.communicationgraphclusteringbackend.db.person.PersonEntity;
import pliska.communicationgraphclusteringbackend.db.person.PersonRepository;

import java.util.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class GraphGenerationService {
    private static final Logger LOGGER = LoggerFactory.getLogger(GraphGenerationService.class);

    private final EmailRepository emailRepository;
    private final NodeRepository nodeRepository;
    private final EdgeRepository edgeRepository;
    private final GraphRepository graphRepository;
    private final PersonRepository personRepository;
    private final LlmService llmService;



    public GraphGenerationService(EmailRepository emailRepository,
                                  NodeRepository nodeRepository,
                                  EdgeRepository edgeRepository,
                                  GraphRepository graphRepository,
                                  PersonRepository personRepository, LlmService llmService) {
        this.emailRepository = emailRepository;
        this.nodeRepository = nodeRepository;
        this.edgeRepository = edgeRepository;
        this.graphRepository = graphRepository;
        this.personRepository = personRepository;

        this.llmService = llmService;


    }

    @Transactional
    public void generateGraphFromEmails(GraphEntity graphEntity) {
        if (graphEntity.getId() == null) {
            graphEntity = graphRepository.save(graphEntity);
        } else {
            Optional<GraphEntity> optionalGraph = graphRepository.findById(graphEntity.getId());
            if (optionalGraph.isPresent()) {
                graphEntity = optionalGraph.get();
            } else {
                graphEntity = graphRepository.save(graphEntity);
            }
        }

        List<EmailEntity> emails = emailRepository.findAll();

        Set<String> senderEmails = extractSenderEmails(emails);

        Map<String, PersonEntity> emailToPersonMap = loadEmailToPersonMap(senderEmails);

        Map<String, NodeEntity> emailToNodeMap = createNodesFromEmails(emails, graphEntity, emailToPersonMap);

        createEdgesFromEmails(emails, graphEntity, emailToNodeMap);
    }

    private Set<String> extractSenderEmails(List<EmailEntity> emails) {
        Set<String> senderEmails = new HashSet<>();

        for (EmailEntity email : emails) {
            String senderEmail = normalizeEmail(email.getFrom());
            if (senderEmail != null) {
                senderEmails.add(senderEmail);
            }

            String toEmailAddress = email.getToEmailAddress();
            if (toEmailAddress != null && !toEmailAddress.trim().isEmpty()) {
                String[] recipients = toEmailAddress.split(",");
                for (String recipient : recipients) {
                    String normalizedRecipient = normalizeEmail(recipient);
                    if (normalizedRecipient != null) {
                        senderEmails.add(normalizedRecipient);
                    }
                }
            }
        }

        return senderEmails;
    }

    private Map<String, PersonEntity> loadEmailToPersonMap(Set<String> senderEmails) {
        Map<String, PersonEntity> emailToPersonMap = new HashMap<>();

        List<PersonEntity> persons = personRepository.findAllByEmailIn(new ArrayList<>(senderEmails));
        for (PersonEntity person : persons) {
            String normalizedEmail = normalizeEmail(person.getEmail());
            if (normalizedEmail != null) {
                emailToPersonMap.put(normalizedEmail, person);
            }
        }

        return emailToPersonMap;
    }

    private Map<String, NodeEntity> createNodesFromEmails(List<EmailEntity> emails,
                                                          GraphEntity graphEntity,
                                                          Map<String, PersonEntity> emailToPersonMap) {
        Map<String, NodeEntity> emailToNodeMap = new HashMap<>();
        List<NodeEntity> nodesBatch = new ArrayList<>();

        LOGGER.info("Starte Erstellung von Nodes aus Emails. Anzahl der Emails: {}", emails.size());

        Set<String> allEmailAddresses = extractSenderEmails(emails);

        List<NodeEntity> existingNodes = nodeRepository.findAllNodesByGraphId(graphEntity.getId());
        Map<Long, NodeEntity> personIdToNodeMap = new HashMap<>();

        for (NodeEntity existingNode : existingNodes) {
            if (existingNode.getPerson() != null && existingNode.getPerson().getId() != null) {
                personIdToNodeMap.put(existingNode.getPerson().getId(), existingNode);
            }
        }

        for (String emailAddress : allEmailAddresses) {
            try {
                LOGGER.debug("Verarbeite Email-Adresse: {}", emailAddress);

                if (emailAddress == null || emailAddress.isEmpty()) {
                    LOGGER.warn("Leere Email-Adresse gefunden. Überspringe.");
                    continue;
                }

                PersonEntity person = emailToPersonMap.get(emailAddress);

                if (person == null) {
                    LOGGER.info("Keine Person mit Email-Adresse {} gefunden. Erstelle neue Person.", emailAddress);

                    NameParts nameParts = extractNameFromEmail(emailAddress);

                    person = new PersonEntity();
                    person.setFirstName(nameParts.firstName());
                    person.setLastName(nameParts.lastName());
                    person.setEmail(emailAddress);

                    person = personRepository.save(person);
                    emailToPersonMap.put(emailAddress, person);

                    LOGGER.debug("Neue Person erfolgreich gespeichert: {}", person);
                }


                NodeEntity existingNode = personIdToNodeMap.get(person.getId());

                if (existingNode != null) {
                    LOGGER.debug("Node für Graph {} und Person {} existiert bereits. Überspringe.",
                            graphEntity.getId(), person.getId());
                    emailToNodeMap.put(emailAddress, existingNode);
                    continue;
                }

                if (!nodeRepository.existsByGraphAndPerson(graphEntity.getId(), person.getId())) {
                    NodeEntity node = new NodeEntity();
                    node.setPerson(person);
                    node.setGraph(graphEntity);
                    node.setClusterId(null);

                    nodesBatch.add(node);

                    if (nodesBatch.size() >= 50) {
                        List<NodeEntity> savedNodes = nodeRepository.saveAll(nodesBatch);
                        for (NodeEntity savedNode : savedNodes) {
                            if (savedNode.getPerson() != null && savedNode.getPerson().getId() != null) {
                                personIdToNodeMap.put(savedNode.getPerson().getId(), savedNode);
                            }
                            if (savedNode.getPerson() != null && savedNode.getPerson().getEmail() != null) {
                                emailToNodeMap.put(normalizeEmail(savedNode.getPerson().getEmail()), savedNode);
                            }
                        }
                        nodesBatch.clear();
                    }
                } else {
                    LOGGER.debug("Node existiert laut existsByGraphAndPerson bereits. Lade ihn aus der vorgeladenen Map.");
                    NodeEntity alreadyExistingNode = personIdToNodeMap.get(person.getId());
                    if (alreadyExistingNode != null) {
                        emailToNodeMap.put(emailAddress, alreadyExistingNode);
                    } else {
                        LOGGER.warn("Node existiert laut existsByGraphAndPerson, konnte aber nicht in existingNodes gefunden werden. PersonId={}", person.getId());
                    }
                }


            } catch (Exception ex) {
                LOGGER.error("Fehler bei der Verarbeitung von Email-Adresse {}. Grund: {}", emailAddress, ex.getMessage(), ex);
            }
        }

        if (!nodesBatch.isEmpty()) {
            List<NodeEntity> savedNodes = nodeRepository.saveAll(nodesBatch);
            for (NodeEntity savedNode : savedNodes) {
                if (savedNode.getPerson() != null && savedNode.getPerson().getId() != null) {
                    personIdToNodeMap.put(savedNode.getPerson().getId(), savedNode);
                }
                if (savedNode.getPerson() != null && savedNode.getPerson().getEmail() != null) {
                    emailToNodeMap.put(normalizeEmail(savedNode.getPerson().getEmail()), savedNode);
                }
            }
        }

        LOGGER.info("Erstellung von Nodes abgeschlossen. Gesamtanzahl erstellter Nodes: {}", emailToNodeMap.size());
        return emailToNodeMap;
    }

    private void createEdgesFromEmails(List<EmailEntity> emails, GraphEntity graphEntity, Map<String, NodeEntity> emailToNodeMap) {


        Map<String, Integer> edgeInteractions = new HashMap<>();
        List<EdgeEntity> edgesBatch = new ArrayList<>();

        for (EmailEntity email : emails) {
            String sender = normalizeEmail(email.getFrom());
            System.out.println("Sender" + sender);

            if (sender == null) {
                LOGGER.warn("Email mit ID {} hat keinen Absender. Überspringe.", email.getId());
                continue;
            }

            String[] recipients = email.getToEmailAddress() == null ? new String[0] : email.getToEmailAddress().split(",");

            for (String recipient : recipients) {
                String normalizedRecipient = normalizeEmail(recipient);

                if (normalizedRecipient == null) {
                    continue;
                }

                String edgeKey = createEdgeKey(sender, normalizedRecipient);


                if (edgeInteractions.containsKey(edgeKey)) {
                    edgeInteractions.put(edgeKey, edgeInteractions.get(edgeKey) + 1);
                } else {
                    edgeInteractions.put(edgeKey, 1);
                }
            }
        }

        int maxInteractions =
                edgeInteractions.values().stream().max(Integer::compare).orElse(1);

        WeightFormula weightFormula = graphEntity.getWeightCalcFormula();


        for (Map.Entry<String, Integer> entry : edgeInteractions.entrySet()) {
            String[] participants = entry.getKey().split("-to-");
            String fromEmail = participants[0];
            String toEmail = participants[1];
            int interactions = entry.getValue();

            NodeEntity sourceNode = emailToNodeMap.get(fromEmail);
            NodeEntity targetNode = emailToNodeMap.get(toEmail);


            if (sourceNode != null && targetNode != null) {
                EdgeEntity edge = new EdgeEntity();
                edge.setGraph(graphEntity);
                edge.setSourceNode(sourceNode);
                edge.setTargetNode(targetNode);
                edge.setInteractions(interactions);
                edge.setWeight(
                        calculateWeight(
                                interactions,
                                maxInteractions,
                                weightFormula,
                                sourceNode,
                                targetNode
                        )
                );




                edgesBatch.add(edge);

                if (edgesBatch.size() >= 50) {
                    edgeRepository.saveAll(edgesBatch);
                    edgesBatch.clear();
                }
            } else {
                LOGGER.warn("Edge konnte nicht erstellt werden. SourceNode oder TargetNode ist null. fromEmail={}, toEmail={}", fromEmail, toEmail);
            }
        }

        if (!edgesBatch.isEmpty()) {
            edgeRepository.saveAll(edgesBatch);
        }


    }

    private String createEdgeKey(String sender, String recipient) {
        if (sender.compareTo(recipient) <= 0) {
            return sender + "-to-" + recipient;
        } else {
            return recipient + "-to-" + sender;
        }
    }

    private String normalizeEmail(String email) {
        if (email == null) {
            return null;
        }

        String normalized = email.trim().toLowerCase();
        return normalized.isEmpty() ? null : normalized;
    }

    private NameParts extractNameFromEmail(String email) {
        String normalizedEmail = normalizeEmail(email);

        if (normalizedEmail == null) {
            return new NameParts("Unknown", "Unknown");
        }

        String localPart = normalizedEmail;
        int atIndex = normalizedEmail.indexOf("@");
        if (atIndex > -1) {
            localPart = normalizedEmail.substring(0, atIndex);
        }

        if (localPart.isBlank()) {
            return new NameParts("Unknown", "Unknown");
        }

        String[] dotParts = localPart.split("\\.");

        if (dotParts.length >= 2) {
            String firstName = capitalizeNamePart(dotParts[0]);
            String lastName = capitalizeNamePart(String.join(" ", Arrays.copyOfRange(dotParts, 1, dotParts.length)));
            return new NameParts(firstName, lastName);
        }

        String[] underscoreParts = localPart.split("_");
        if (underscoreParts.length >= 2) {
            String firstName = capitalizeNamePart(underscoreParts[0]);
            String lastName = capitalizeNamePart(String.join(" ", Arrays.copyOfRange(underscoreParts, 1, underscoreParts.length)));
            return new NameParts(firstName, lastName);
        }

        String[] hyphenParts = localPart.split("-");
        if (hyphenParts.length >= 2) {
            String firstName = capitalizeNamePart(hyphenParts[0]);
            String lastName = capitalizeNamePart(String.join(" ", Arrays.copyOfRange(hyphenParts, 1, hyphenParts.length)));
            return new NameParts(firstName, lastName);
        }

        return new NameParts("Unknown", capitalizeNamePart(localPart));
    }

    private String capitalizeNamePart(String value) {
        if (value == null) {
            return "Unknown";
        }

        String cleaned = value.trim().replaceAll("[^a-zA-ZäöüÄÖÜß ]", " ").replaceAll("\\s+", " ").trim();

        if (cleaned.isEmpty()) {
            return "Unknown";
        }

        String[] parts = cleaned.split(" ");
        StringBuilder result = new StringBuilder();

        for (String part : parts) {
            if (part.isBlank()) {
                continue;
            }

            if (!result.isEmpty()) {
                result.append(" ");
            }

            result.append(part.substring(0, 1).toUpperCase());
            if (part.length() > 1) {
                result.append(part.substring(1).toLowerCase());
            }
        }

        return result.isEmpty() ? "Unknown" : result.toString();
    }

    private record NameParts(String firstName, String lastName) {
    }


    private HashMap<String, Double>  getLlmWeights(){
        HashMap<String, Double> llmWeights=new HashMap<>();
        llmWeights.put("personal", 2.0);
        llmWeights.put("topic", 2.0);
        llmWeights.put("sentiments", 1.0);
        llmWeights.put("interactions", 3.0);
        return llmWeights;
    }

    private Double calculateWeight(
            int interactions, int maxInteractions, WeightFormula weightFormula, NodeEntity sourceNode, NodeEntity targetNode) {
        switch (weightFormula) {
            case LOG_XMAX_WEIGHT:
                return WeightCalculator.logXmaxWeight(interactions, maxInteractions);
            case LOG_WEIGHT:
                return WeightCalculator.logWeight(interactions);
            case LOG_XMAX_WEIGHT_LLM:
                return WeightCalculator.logXmaxWeightLLM(getLlmWeights(),llmService.calculateLlmValues(sourceNode.getPerson().getEmail(),targetNode.getPerson().getEmail()),interactions,maxInteractions);
            case LOG_WEIGHT_LLM:
                return WeightCalculator.logWeightLlm(getLlmWeights(),llmService.calculateLlmValues(sourceNode.getPerson().getEmail(),targetNode.getPerson().getEmail()),interactions);
            default:
                LOGGER.warn("Unknown WeightFormula: " + weightFormula);
                return 0.0;
        }
    }

}