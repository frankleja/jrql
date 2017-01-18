package Full.Publishing;

import com.hlcl.rql.as.CmsClient;
import com.hlcl.rql.as.LanguageVariant;
import com.hlcl.rql.as.Page;
import com.hlcl.rql.as.PageAlreadyInPublishingQueueException;
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
 * @author Ibrahim Sawadogo
 *
 */
public class AllProjects {

    private static final Logger logger = Logger.getLogger(AllProjects.class);

    static boolean logIt = false;
    static boolean dryRun = false;

    static CmsClient client = null;
    private static File targetFile;
    private static Properties properties;

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

                String seperator = ":";
                String allProjectVariantGuids = getAllProjectVariantGuids(project, seperator);
                boolean withFollowingPages = true;
                boolean checkInPublicationPackage = false;

                try {
                    PageArrayList allPagesWithFilename = project.getAllPagesWithFilename();
                    //allPagesWithFilename.releaseAll();

                    for (Page PageWithFilename : allPagesWithFilename) {
                        String pageWithFilenamePgID = PageWithFilename.getPageId();

                        logger.info(MessageFormat.format("#Publishing PageID {0}", pageWithFilenamePgID));
                        if (dryRun) {
                            //do nothing
                        } else {
                            try {
                                PageWithFilename.publishAllCombinationsAllLanguageVariants(withFollowingPages, allProjectVariantGuids, seperator, checkInPublicationPackage);
                            }catch (RQLException ex) {
                                logger.error(MessageFormat.format("Exception: {0}\n", ex));
                                Throwable re = ex.getCause();
                                if (re != null) {
                                    logger.error(MessageFormat.format("Reason: {0}\n Message: {1}\n", re, re.getMessage()));
                                }
                            } //catch
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
