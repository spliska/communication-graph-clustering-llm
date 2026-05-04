package pliska.communicationgraphclusteringbackend.db.person;

public class TitleToLevelParser {
    public static int parseTitleToLevel(String title) {
        if (title == null || title.isBlank()) {
            return 0;
        }

        String t = title.toLowerCase();

        if (t.contains("ceo")
                || t.contains("chief executive")
                || t.contains("president")) {
            return 5;
        }

        if (t.contains("coo")
                || t.contains("cfo")
                || t.contains("cto")
                || t.contains("chief")
                || t.contains("managing director")
                || t.contains("mng director")
                || t.contains("mng dir")) {
            return 4;
        }

        if (t.contains("vice president")
                || t.contains("vp")
                || t.contains("v.p")) {
            return 4;
        }

        if (t.contains("director")
                || t.contains("dir")) {
            return 3;
        }

        if (t.contains("manager")
                || t.contains("mgr")) {
            return 2;
        }

        if (t.contains("trader")
                || t.contains("attorney")
                || t.contains("counsel")
                || t.contains("cnsl")
                || t.contains("specialist")
                || t.contains("analyst")
                || t.contains("associate")
                || t.contains("administrator")
                || t.contains("assistant")
                || t.contains("employee")) {
            return 1;
        }

        return 0;
    }
}
