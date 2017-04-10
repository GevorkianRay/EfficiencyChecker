import java.io.File;
import java.lang.reflect.*;
import java.util.HashSet;
import java.util.Set;
/**
 * @Author: Raymond Gevorkian
 * DA displays a table of the classes of a given project with their respective metrics:
 * 1) instability - the amount of classes one class depends on divided by the total number of classes.
 * 2) responsibility - the amount of classes that are dependent on that class divided by the total number of classes.
 * 3) inDepth - the amount of super classes a specific class has, excluding the Object class.
 * 4) workLoad - the amount of methods a specific class has divided by the total number of methods in the package.
 */
public class DA
{
    Set<Class> classes = new HashSet<Class>(); //Initializes a hash set of type Class.
    /**
     * Loads the package and saves the classes into the hash set classes. 
     * @param path - the String that represents the path to the directory of the package.
     * @throws Exception - if the file does not exist.
     */
    public void loadPackage(String path) throws Exception
    {
        File[] packageContents = new File(path).listFiles();
        if (packageContents != null) //If there are files in the package
        {
            for (File file : packageContents) //For each file in the given package
            {
                String fileName = file.getName();
                if (fileName.contains(".class"))
                {
                    /**
                     * Dropping the extension and prepending the package name.
                     * Then, adding the classes from the package into the hash set.
                     */
                    int endSymbol = path.lastIndexOf("/");
                    String packagePath = path.substring(endSymbol + 1);
                    String fixedName = file.getName().substring(0, file.getName().length() - 6); //dropping extension.
                    Class c = Class.forName(packagePath + "." + fixedName); //prepending package name.
                    classes.add(c);
                }
            }
        }
    }
    /**
     * Displays the metrics of the Design Analyzer class formatted into a table.
     * Attributions to a peer for helping me with formatting ( though I was told it is not required ). 
     */
    public void displayMetrics()
    {
        //Prints the first row of the table.
        System.out.println("Class(C)          inDepth(C)          instability(C)          responsibility(C)          workload(C)");
        //Setting up the formatting based off the size of each String, to two decimals where needed.
        String setter = "%-8s          %-10d          %-14.2f          %-17.2f          %-11.2f\n";
        //Find the metrics for each Class c in the class list from the given package.
        for(Class c: classes)
        {
            String className = c.getSimpleName();
            System.out.printf(setter, className, inDepth(c), instability(c), responsibility(c), workLoad(c));
        }
    }
    /**
     * Calculates the depth of a class by finding the amount of super classes a specific class has.
     * @param c - the Class to check
     * @return the inDepth.
     */
    private int inDepth(Class c)
    {
        if (c == null) return -1;
        else  return inDepth(c.getSuperclass()) + 1;
    }
    /**
     * Calculates the amount of methods a certain class has divided by the total amount of classes in the package.
     * @param c - the Class to calculate the workLoad of.
     * @return the workLoad.
     */
    private double workLoad(Class c)
    {
        int totalProjMethods = 0;
        int totalClassMethods = c.getDeclaredMethods().length; //gets the number of declared methods in a given Class.
        for(Class cls : classes)
        {
            totalProjMethods += cls.getDeclaredMethods().length; //gets the number of declared methods in the entire set of classes.
        }
        return (double) totalClassMethods / totalProjMethods;
    }
    /**
     * Calculates the instability of the metrics.
     * @param c - The Class to calculate the instability of.
     * @return instability - the amount of classes one class depends on divided by the total number of classes.
     */
    private double instability(Class c)
    {
        double classCount = classes.size();
        Set<Class> providers = new HashSet<>(); //the amount of classes one specific class depends on.
        for (Class cls : classes)
        {
            /**
             * Checking inheritance of a class.
             * Checks if the superclass of a given class match the name of any of the classes in the package.
             */
            if (c.getSuperclass().getName().equals(cls.getName()))
                providers.add(c.getSuperclass());
            /**
             * Checking the fields of each class in the package.
             * Does the field of a class match the name of any of the classes in a package?
             */
            for (Field clsField : c.getDeclaredFields())
            {
                if (clsField.getType().getName().equals(cls.getName())&& !clsField.getType().getName().equals(c.getName()))
                    providers.add(clsField.getType());
            }
            /**
             * Checking the interface of each class in the package.
             * Does the interface of a class match the name of any of the classes in the package?
             */
            for (Class clsInterface : c.getInterfaces())
            {
                if (clsInterface.getName().equals(cls.getName()))
                    providers.add(clsInterface);
            }
            /**
             * Checking the methods of each class in the package.
             * Checking the parameters of each of the methods.
             * Does the parameters of a method equals the name of any of the classes in the package?
             */
            for (Method clsMethod : c.getDeclaredMethods())
            {
                for (Class clsParam : clsMethod.getParameterTypes())
                {
                    if (clsParam.getName().equals(cls.getName())&& !clsParam.getName().equals(c.getName()))
                        providers.add(clsParam);
                }
            }
        }
        return providers.size() / classCount;
    }
    /**
     * Calculates the responsibility of the metrics.
     * @param c - The Class to calculate the instability of.
     * @return responsibility - the amount of classes dependent on this class divided by the total number of classes.
     */
    private double responsibility(Class c)
    {
        double classCount = classes.size();
        double count = 0;
        for(Class cls : classes)
        {
            Set<Class> clients = new HashSet<>();
            /**
             * Checking inheritance of each class. 
             * Determines if it is a super class of any given class.
             */
            if (cls.getSuperclass().getName().equals(c.getName()))
                clients.add(c.getSuperclass());
            /**
             * Checking fields of each class.
             * Determines if it is a field of any given class.
             */
            for (Field claField : cls.getDeclaredFields())
            {
                if (claField.getType().getName().equals(c.getName())&& !claField.getType().getName().equals(cls.getName()))
                    clients.add(claField.getType());
            }
            /**
             * Checking interfaces of each class.
             * Determines if it is an interface of any given class.
             */
            for (Class clsInterface : c.getInterfaces())
            {
                if (clsInterface.getName().equals(c.getName()))
                    clients.add(clsInterface);
            }
            /**
             * Checking methods of each class.
             * Determines if the parameter of a method matches the given class.
             */
            for (Method clsMethod : cls.getDeclaredMethods())
            {
                for (Class clsParam : clsMethod.getParameterTypes())
                {
                    if (clsParam.getName().equals(c.getName())&& !clsParam.getName().equals(cls.getName()))
                        clients.add(clsParam);
                }
            }
            count += clients.size();
        }
        return count / classCount;
    }
    /**
     * Runs the program.
     * @param args - The path to the package
     * @throws Exception - IOException, FileNotFoundException, ClassNotFoundException
     */
    public static void main(String[] args) throws Exception {
        DA da = new DA();
        da.loadPackage(args[0]);
        da.displayMetrics();
    }
}
