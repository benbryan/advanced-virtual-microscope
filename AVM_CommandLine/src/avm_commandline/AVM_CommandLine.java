package avm_commandline;

import avl.sv.shared.AVM_Properties;
import avl.sv.shared.model.featureGenerator.jocl.JOCL_Configure;
import avl.sv.shared.model.featureGenerator.jocl.Platform;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

public class AVM_CommandLine {

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        System.out.println("AVM Command line interface: type help for options");
        while (true) {
            System.out.print("> ");
            String selection = scanner.next();
            if (selection.toLowerCase().equals("help")) {
                System.out.println("Available options");
                System.out.println("   Functions");
                System.out.println("      help");
                System.out.println("      exit");
                System.out.println("      OpenCL");
                System.out.println("   Key value pairs (Key -> Value)");
                for (AVM_Properties.Name name : AVM_Properties.Name.values()) {
                    System.out.println("      " + name.name() + " -> " + AVM_Properties.getProperty(name));
                }
            } else if (selection.toLowerCase().equals("exit")) {
                System.exit(0);
            } else if (selection.toLowerCase().equals("opencl")) {
                openCL();
            } else {
                AVM_Properties.Name selectedName;
                try {
                    selectedName = AVM_Properties.Name.valueOf(selection);
                } catch (IllegalArgumentException ex){
                    System.out.println("invalud option");
                    continue;
                }
                String value = AVM_Properties.getProperty(selectedName);
                if (value == null){
                    System.out.println("   value not set");
                } else if (value.isEmpty()){
                    System.out.println("   value is empty");
                } else {
                    System.out.println("   value is currently " + value);
                }
                System.out.print("new value?>");
                String newValue = scanner.next();
                AVM_Properties.setProperty(selectedName, newValue);
                System.out.println("property set");
            }
        }
    }

    private static void openCL() {
        Scanner scanner = new Scanner(System.in);
        ArrayList<Platform> platforms = JOCL_Configure.getPlatforms();
        if (platforms.isEmpty()) {
            System.out.println("No OpenCL platforms found");
            System.exit(0);
        }
        int platformSelected = JOCL_Configure.getSelectedPlatformIndex();
        int deviceSelected = JOCL_Configure.getSelectedDeviceIndex();
        System.out.println("OpenCL setup: type help for options");
        while (true) {
            System.out.print("> ");
            switch (scanner.next()) {
                case "platform":
                    System.out.println("Select a platform by ID");
                    System.out.println("  ID, Name");
                    for (int i = 0; i < platforms.size(); i++) {
                        String pre;
                        if (i == platformSelected) {
                            pre = "* ";
                        } else {
                            pre = "  ";
                        }
                        System.out.println(pre + String.valueOf(i) + ",  " + platforms.get(i).platformName);
                    }
                    System.out.print("> ");
                    try {
                        int temp = Integer.parseInt(scanner.next());
                        if ((temp < 0) || (temp > platforms.size() - 1)) {
                            System.out.println("Invalid ID");
                            continue;
                        }
                        platformSelected = temp;
                    } catch (NumberFormatException ex) {
                        System.out.println("Invalid selection");
                        continue;
                    }
                case "device":
                    if ((platformSelected < 0) || (platformSelected > platforms.size() - 1)) {
                        System.out.println("Select a valid platform first");
                        continue;
                    }
                    System.out.println("Select a device by ID");
                    System.out.println("ID, Name");
                    for (int i = 0; i < platforms.get(platformSelected).devices.size(); i++) {
                        String pre;
                        if (i == deviceSelected) {
                            pre = "* ";
                        } else {
                            pre = "  ";
                        }
                        System.out.println(pre + String.valueOf(i) + "  " + platforms.get(platformSelected).devices.get(i).deviceName);
                    }
                    System.out.print("> ");
                    try {
                        int temp = Integer.parseInt(scanner.next());
                        if ((temp < 0) || (temp > platforms.size() - 1)) {
                            System.out.println("Invalid ID");
                            continue;
                        }
                        deviceSelected = temp;
                    } catch (NumberFormatException ex) {
                        System.out.println("Invalid selection");
                    }
                    break;
                case "help":
                    System.out.println("Available options");
                    System.out.println("   platform");
                    System.out.println("   device");
                    System.out.println("   help");
                    System.out.println("   return");
                    System.out.println("   exit");
                    break;
                case "return":
                    return;
                case "exit":
                    System.exit(0);
                    break;
                default:
                    System.out.println("invalud option");
            }
        }
    }

}
