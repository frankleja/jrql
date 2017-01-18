package Full.Publishing;

import com.hlcl.rql.as.CmsClient;
import com.hlcl.rql.as.LanguageVariant;
import com.hlcl.rql.as.Page;
import com.hlcl.rql.as.PasswordAuthentication;
import com.hlcl.rql.as.Project;
import com.hlcl.rql.as.RQLException;
import com.hlcl.rql.as.User;
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

    boolean logIt = false;

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

        String logonGuid = "";
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

            User connectedUser = client.getConnectedUser();
            List<Project> allProjects = client.getAllProjects();

            for (Project nextProject : allProjects) {

                String allProjectVariantGuids = getAllProjectVariantGuids(nextProject);

                String nextProjectGuids = nextProject.getProjectGuid();
                boolean withFollowingPages = true;
                boolean checkInPublicationPackage = false;

                PageArrayList allPagesWithFilename = nextProject.getAllPagesWithFilename();
                allPagesWithFilename.releaseAll();

                for (Page PageWithFilename : allPagesWithFilename) {
                    String pageWithFilenamePgID = PageWithFilename.getPageId();

                    PageWithFilename.publishAllCombinationsAllLanguageVariants(withFollowingPages, allProjectVariantGuids, ":", checkInPublicationPackage);
                }

            }

            client.changeCurrentProjectByName(projectName);
            logonGuid = client.getLogonGuid();
            projectGuid = client.getCurrentProjectGuid();

            project = client.getProject(sessionKey, projectGuid);
            allLangVariants = project.getAllLanguageVariants();

            /* end of logic */
        } catch (RQLException ex) {
            logger.warn(MessageFormat.format("Got exception", ex));
            String error = "";
            Throwable re = ex.getReason();
            if (re != null) {
                error += re.getMessage();
            }
        } finally {
            //client.disconnect();
        }
        client.disconnect();
        logger.info("End of Java Program");
    }

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

    }

    private static String getAllProjectVariantGuids(Project nextProject) throws RQLException {
        String allProjectVariantGuids = "";
        List<LanguageVariant> allLanguageVariants = nextProject.getAllLanguageVariants();
        for (LanguageVariant languageVariant : allLanguageVariants) {
            languageVariant.getLanguageVariantGuid();
        }
        return allProjectVariantGuids;
    }
}
