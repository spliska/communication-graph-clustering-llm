package pliska.communicationgraphclusteringbackend.loader.metadata;

import com.google.gson.Gson;
import org.springframework.stereotype.Service;
import pliska.communicationgraphclusteringbackend.db.metrics.GroundTruthSourceRepository;
import pliska.communicationgraphclusteringbackend.db.person.*;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;



@Service
public class OrganizationalDataImporter {

    private final GroundTruthPersonRepository groundTruthPersonRepository;
    private final GroundTruthSourceRepository groundTruthSourceRepository;

    public OrganizationalDataImporter(GroundTruthPersonRepository groundTruthPersonRepository,
                                      GroundTruthSourceRepository groundTruthSourceRepository) {
        this.groundTruthPersonRepository = groundTruthPersonRepository;
        this.groundTruthSourceRepository = groundTruthSourceRepository;
    }

    public boolean importData(String fileName) throws IOException {
        String sourceName = fileName;
        GroundTruthSource source = groundTruthSourceRepository.findByName(sourceName)
                .orElseGet(() -> {
                    GroundTruthSource newSource = new GroundTruthSource();
                    newSource.setName(sourceName);
                    return groundTruthSourceRepository.save(newSource);
                });

        List<GroundTruthPersonEntity> persons = new ArrayList<>();
        if (fileName.endsWith(".json")) {
            persons = importFromJson(fileName, source);
        } else if (fileName.endsWith(".txt")) {
            persons = importFromTxt(fileName, source);
        }

        groundTruthPersonRepository.saveAll(persons);
        return true;
    }

    private List<GroundTruthPersonEntity> importFromJson(String fileName, GroundTruthSource source) throws IOException {
        Gson gson = new Gson();
        List<GroundTruthPersonEntity> persons = new ArrayList<>();

        try (FileReader reader = new FileReader(fileName)) {
            Type listType = new TypeToken<List<EmployeeJsonModel>>() {}.getType();
            List<EmployeeJsonModel> employeeData = gson.fromJson(reader, listType);

            for (EmployeeJsonModel employee : employeeData) {
                GroundTruthPersonEntity person = new GroundTruthPersonEntity(
                        null,
                        source,
                        employee.getFirstName(),
                        employee.getLastName(),
                        employee.getTitle(),
                        employee.getDepartment(),
                        employee.getLongDepartment()
                );
                persons.add(person);
            }
        }

        return persons;
    }


    private List<GroundTruthPersonEntity> importFromTxt(String fileName, GroundTruthSource source) throws IOException {
        List<GroundTruthPersonEntity> persons = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(fileName))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split("\t");
                if (parts.length >= 4) {
                    String username = parts[0];
                    String fullName = parts[1];
                    String title = parts[2];
                    String department = parts.length > 3 ? parts[3] : null;

                    String[] nameParts = fullName.split(" ");
                    String firstName = nameParts[0];
                    String lastName = nameParts.length > 1 ? nameParts[nameParts.length - 1] : "";

                    GroundTruthPersonEntity person = new GroundTruthPersonEntity(
                            null,
                            source,
                            firstName,
                            lastName,
                            title,
                            department,
                            null
                    );
                    persons.add(person);
                }
            }
        }
        return persons;
    }
}