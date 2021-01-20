package org.hadatac.console.models;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import controllers.Assets.Asset;
import controllers.routes;

public class FileTemplate {

    public static String GENERIC = "GENERIC";
    public static String HHEAR = "HHEAR";

    public static List<String> TEMPLATETYPES = new ArrayList<String>(Arrays.asList(GENERIC, HHEAR));

    public String name;
    public String template;
    public String path;

    public static FileTemplate DPL_GEN = new FileTemplate("DPL", "GENERIC", routes.Assets.versioned(new Asset("example/data/templates/generic/DPL.xlsx")).toString());
    public static FileTemplate STD_GEN = new FileTemplate("STD", "GENERIC", routes.Assets.versioned(new Asset("example/data/templates/generic/STD.xlsx")).toString());
    public static FileTemplate SSD_GEN = new FileTemplate("SSD", "GENERIC", routes.Assets.versioned(new Asset("example/data/templates/generic/SSD.xlsx")).toString());
    public static FileTemplate SDD_GEN = new FileTemplate("SDD", "GENERIC", routes.Assets.versioned(new Asset("example/data/templates/generic/SDD.xlsx")).toString());
    public static FileTemplate STR_GEN = new FileTemplate("STR", "GENERIC", routes.Assets.versioned(new Asset("example/data/templates/generic/STR.csv")).toString());
    public static FileTemplate DPL_HHEAR = new FileTemplate("DPL", "HHEAR", routes.Assets.versioned(new Asset("example/data/templates/hhear/DPL.xlsx")).toString());
    public static FileTemplate STD_HHEAR = new FileTemplate("STD", "HHEAR", routes.Assets.versioned(new Asset("example/data/templates/hhear/STD.xlsx")).toString());
    public static FileTemplate SSD_HHEAR = new FileTemplate("SSD", "HHEAR", routes.Assets.versioned(new Asset("example/data/templates/hhear/SSD.xlsx")).toString());
    public static FileTemplate SDD_HHEAR = new FileTemplate("SDD", "HHEAR", routes.Assets.versioned(new Asset("example/data/templates/hhear/SDD.xlsx")).toString());
    public static FileTemplate STR_HHEAR = new FileTemplate("STR", "HHEAR", routes.Assets.versioned(new Asset("example/data/templates/hhear/STR.csv")).toString());

    public static List<FileTemplate> FILETEMPLATES = Arrays.asList(
            DPL_GEN, STD_GEN, SSD_GEN, SDD_GEN, STR_GEN,
            DPL_HHEAR, STD_HHEAR, SSD_HHEAR, SDD_HHEAR, STR_HHEAR);

    public FileTemplate(String name, String template, String path) {
        this.name = name;
        this.template = template;
        this.path = path;
    }

    public static FileTemplate find(String name, String template) {
        for (FileTemplate tp : FILETEMPLATES) {
            if (tp.getName().equals(name) && tp.getTemplate().equals(template)) {
                return tp;
            }
        }
        return null;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTemplate() {
        return template;
    }

    public void setTemplate(String template) {
        this.template = template;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

}
