package Full.Publishing;

import com.hlcl.rql.as.CmsClient;
import com.hlcl.rql.as.Page;
import com.hlcl.rql.as.PasswordAuthentication;
import com.hlcl.rql.as.Project;
import com.hlcl.rql.as.ProjectVariant;
import com.hlcl.rql.as.RQLException;
import com.hlcl.rql.util.as.PageArrayList;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.List;
import java.util.Properties;
import org.apache.log4j.Logger;

/**
 * @author Ibrahim Sawadogo (http://IbrahimSawadogo.pro)
 * 
 * this script publishes all pages in all projects on the server.
 *
 */
public class AllProjects {

    private static final Logger logger = Logger.getLogger(AllProjects.class);

    static CmsClient client = null;
    private static File targetFile;
    private static Properties properties;

    static boolean logIt = true;
    static boolean dryRun = true;

    static String beforeFilename = "b4.txt";
    static String afterFilename = "af.txt";

    static {
        targetFile = new File("./password.txt");
        properties = new Properties();
        try {
            properties.load(
                    new FileInputStream(targetFile.getAbsolutePath()));
        } catch (IOException ioe) {
            System.err.println(
                    "Unable to read file.");
        }
    }

    /**
     * @throws RQLException
     */
    public static void main(String[] args) throws RQLException {
        String user = "";
        String pw = "";

        String sessionKey = "";
        String projectGuid = "";;

        Project project = null;

        //load CMS login Credentials from File
        for (String key : properties.stringPropertyNames()) {
            user = key;
            pw = properties.getProperty(key);
        }

        try {
            client = new CmsClient(new PasswordAuthentication(user, pw));

            sessionKey = client.getLogonGuid();
            client.noCurrentProject();

            List<Project> allProjects = client.getAllProjects();

            for (Project nextProject : allProjects) {
                projectGuid = nextProject.getProjectGuid();

                project = client.getProject(sessionKey, projectGuid);
                String projectName = project.getName();
                logger.info(MessageFormat.format("\n\n#Project Name: {0}", projectName));
                if (logIt) {
                    appendToFile(MessageFormat.format("\n\n#Project Name: {0}", projectName), afterFilename);
                }

                String seperator = ":";
                String allProjectVariantGuids = getAllProjectVariantGuids(project, seperator);
                boolean withFollowingPages = true;
                boolean checkInPublicationPackage = false;

                try {
                    PageArrayList allPagesWithFilename = project.getAllPagesWithFilename();

                    for (Page PageWithFilename : allPagesWithFilename) {
                        String pageWithFilenamePgID = PageWithFilename.getPageId();

                        logger.info(MessageFormat.format("#Publishing PageID {0}", pageWithFilenamePgID));
                        if (logIt) {
                            appendToFile(MessageFormat.format("##Publishing PageID {0}", pageWithFilenamePgID), afterFilename);
                        }
                        if (!dryRun) {
                            try {
                                PageWithFilename.publishAllCombinationsAllLanguageVariants(withFollowingPages, allProjectVariantGuids, seperator, checkInPublicationPackage); //dryRun
                            } catch (RQLException ex) {
                                logger.error(MessageFormat.format("Exception: {0}\n", ex));
                                if (logIt) {
                                    appendToFile(MessageFormat.format("###PageID {0} has an error {1}", pageWithFilenamePgID, ex), afterFilename);
                                }
                                Throwable re = ex.getCause();
                                if (re != null) {
                                    logger.error(MessageFormat.format("Reason: {0}\n Message: {1}\n", re, re.getMessage()));
                                }
                            } //catch
                        } else {
                            //do nothing
                        } //else
                    } //PageWithFilename

                } catch (Exception ex) {
                    logger.error(MessageFormat.format("Exception: {0}\n", ex));
                    Throwable re = ex.getCause();
                    if (re != null) {
                        logger.error(MessageFormat.format("Reason: {0}\n Message: {1}\n", re, re.getMessage()));
                    }
                }
            } //nextProject

            /* end of logic */
        } catch (RQLException ex) {
            logger.error(MessageFormat.format("Exception: {0}\n", ex));
            Throwable re = ex.getReason();
            if (re != null) {
                logger.error(MessageFormat.format("Reason: {0}\n Message: {1}\n", re, re.getMessage()));
            }
        } finally {
        }
        client.disconnect();
        logger.info("End of Java Program");
    }

    private static String getAllProjectVariantGuids(Project project, String seperator) throws RQLException {
        String allProjectVariantGuids = "";
        List<ProjectVariant> allProjectVariants = project.getAllProjectVariants();
        for (ProjectVariant projectVariant : allProjectVariants) {
            int index = allProjectVariants.indexOf(projectVariant);
            if (index == 0) {
                allProjectVariantGuids = projectVariant.getProjectVariantGuid();
            } else {
                allProjectVariantGuids = allProjectVariantGuids + seperator + projectVariant.getProjectVariantGuid();
            }
        }
        return allProjectVariantGuids;
    } //getAllProjectVariantGuids

    public static void appendToFile(String content, String filename) {

        String FILENAME = filename;
        BufferedWriter bw = null;
        FileWriter fw = null;

        try {
            String data = content;
            File file = new File(FILENAME);

            // if file doesnt exists, then create it
            if (!file.exists()) {
                file.createNewFile();
            }

            // true = append file
            fw = new FileWriter(file.getAbsoluteFile(), true);
            bw = new BufferedWriter(fw);
            bw.write(data);

            //System.out.println("Done");
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (bw != null) {
                    bw.close();
                }
                if (fw != null) {
                    fw.close();
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    } //appendToFile
} //end if class
