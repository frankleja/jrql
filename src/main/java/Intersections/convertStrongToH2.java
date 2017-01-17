package Intersections;

import com.hlcl.rql.as.TextElement;
import com.hlcl.rql.as.CmsClient;
import com.hlcl.rql.as.LanguageVariant;
import com.hlcl.rql.as.Page;
import com.hlcl.rql.as.PasswordAuthentication;
import com.hlcl.rql.as.Project;
import com.hlcl.rql.as.RQLException;
import com.hlcl.rql.as.Template;
import com.hlcl.rql.util.as.PageArrayList;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Ibrahim Sawadogo
 *
 */
public class convertStrongToH2 {

    private static Logger logger = Logger.getLogger("ibrahim");

    static CmsClient client = null;
    private static File targetFile;
    private static Properties properties;

    static {
        targetFile = new File("./password.txt");
        properties = new Properties();
        try {
            properties.load(
                    new FileInputStream(
                            targetFile.getAbsolutePath()));
        } catch (IOException ioe) {
            System.err.println(
                    "Unable to read file.");
        }
    }

    /**
     * @param projects
     * @throws RQLException
     */
    public static void main(String[] args) throws RQLException {
        String user = "";
        String pw = "";

        String sessionKey = "";
        String projectGuid = "";
        String projectName = "GIZ Master";

        List<LanguageVariant> allLangVariants = new ArrayList();
        PageArrayList allPagesInCurrentTemplate;
        Project project = null;

        try {
            FileHandler fh = new FileHandler("log.txt");
            Logger.getLogger("ibrahim").addHandler(fh);
        } catch (IOException ex) {
            Logger.getLogger(convertStrongToH2.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SecurityException ex) {
            Logger.getLogger(convertStrongToH2.class.getName()).log(Level.SEVERE, null, ex);
        }

        //load CMS login Credentials from File
        for (String key : properties.stringPropertyNames()) {
            user = key;
            pw = properties.getProperty(key);
        }

        try {
            client = new CmsClient(new PasswordAuthentication(user, pw));
            client.changeCurrentProjectByName(projectName);
            projectGuid = client.getCurrentProjectGuid();

            project = client.getProject(sessionKey, projectGuid);
            allLangVariants = project.getAllLanguageVariants();

            /* set language variant */
            int allLanVariantsSize = allLangVariants.size();
            for (int alv = 0; alv < allLanVariantsSize; alv++) {
                project.setCurrentLanguageVariant(allLangVariants.get(alv));

                logger.log(Level.INFO, "#set Current Language Variant to {0}", allLangVariants.get(alv).getName());
                System.out.println("\n\n#set Current Language Variant to " + allLangVariants.get(alv).getName());

                List<Template> allTemplatesInProject = project.getAllTemplates();

                int allTemplatesInProjectSize = allTemplatesInProject.size();
                logger.log(Level.INFO, "##allTemplatesInProjectSize {0}", allTemplatesInProjectSize);

                /* loop over "all Templates In 1 Project" */
                for (Template nextTemplate : allTemplatesInProject) {
                    allPagesInCurrentTemplate = nextTemplate.getAllPages(9999);

                    int allPagesInCurrentTemplateSize = allPagesInCurrentTemplate.size();
                    logger.log(Level.INFO, "###allPagesInCurrentTemplateSize {0}", allPagesInCurrentTemplateSize);

                    /* loop over "all Pages in 1 Template" */
                    for (int pit = 0; pit < allPagesInCurrentTemplateSize; pit++) {
                        Page currentPgInCurrentTemplate = allPagesInCurrentTemplate.get(pit);
                        String currentPgInCurrentTemplatePageId = currentPgInCurrentTemplate.getPageId();

                        if (currentPgInCurrentTemplate.existsInCurrentLanguageVariant()) {
                            logger.log(Level.INFO, "####PageID in current Template {0}", currentPgInCurrentTemplatePageId);
                            List<TextElement> filledTextElements = currentPgInCurrentTemplate.getFilledTextElements();

                            /* loop over "filled TextElements in 1 page" */
                            for (TextElement filledElement : filledTextElements) {

                                //string.setText(findAndReplaceAll(string.getText())); //dryrun
                                logger.log(Level.INFO, "\n####{0}", findAndReplaceAll(filledElement.getText()));

                            } //filledTextElements
                        } //exisit in current lang variant of currentPgInCurrentTemplate
                        else {
                            logger.log(Level.INFO, "####PageID {0} does not exist in current lang variant", currentPgInCurrentTemplatePageId);
                        }

                    } //pit
                    allPagesInCurrentTemplate.clear();
                } //lang variant
            } //allTemplatesInProject
            /* end of logic */

        } catch (RQLException ex) {
            logger.log(Level.WARNING, "Got exception", ex);
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

    private static String findAndReplaceAll(String text) {

        text = text.replaceAll("<strong>", "<h2>").replaceAll("<h2><h2>", "<h2><b>");
        text = text.replaceAll("</strong>", "</h2>").replaceAll("</h2></h2>", "</b></h2>");

        return text;
    }
}
