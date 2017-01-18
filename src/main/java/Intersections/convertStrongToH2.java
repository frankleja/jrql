package Intersections;

import com.hlcl.rql.as.CmsClient;
import com.hlcl.rql.as.LanguageVariant;
import com.hlcl.rql.as.Page;
import com.hlcl.rql.as.PasswordAuthentication;
import com.hlcl.rql.as.Project;
import com.hlcl.rql.as.RQLException;
import com.hlcl.rql.as.Template;
import com.hlcl.rql.as.TextElement;
import com.hlcl.rql.util.as.PageArrayList;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import org.apache.log4j.Logger;

/**
 * @author Ibrahim Sawadogo
 *
 */
public class convertStrongToH2 {

    private static final Logger logger = Logger.getLogger(convertStrongToH2.class);

    static CmsClient client = null;
    private static File targetFile;
    private static Properties properties;
    static boolean dryRun = true;

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
        String projectName = "GIZ Master";

        List<LanguageVariant> allLangVariants = new ArrayList();
        PageArrayList allPagesInCurrentTemplate;
        Project project = null;

        //load CMS login Credentials from File
        for (String key : properties.stringPropertyNames()) {
            user = key;
            pw = properties.getProperty(key);
        }

        try {
            client = new CmsClient(new PasswordAuthentication(user, pw));
            client.changeCurrentProjectByName(projectName);
            logonGuid = client.getLogonGuid();
            projectGuid = client.getCurrentProjectGuid();

            project = client.getProject(sessionKey, projectGuid);
            allLangVariants = project.getAllLanguageVariants();

            /* set language variant */
            int allLanVariantsSize = allLangVariants.size();
            for (int alv = 0; alv < allLanVariantsSize; alv++) {
                project.setCurrentLanguageVariant(allLangVariants.get(alv));

                String CurrentLanguageVariantName = allLangVariants.get(alv).getName();
                System.out.println("\n\n#Language Variant " + CurrentLanguageVariantName);
                appendToFile(MessageFormat.format("##Language Variant {0}\n", CurrentLanguageVariantName), "beforeChange.txt");
                appendToFile(MessageFormat.format("##Language Variant {0}\n", CurrentLanguageVariantName), "afterChange.txt");

                List<Template> allTemplatesInProject = project.getAllTemplates();

                int allTemplatesInProjectSize = allTemplatesInProject.size();
                logger.info(MessageFormat.format("##allTemplatesInProjectSize {0}", allTemplatesInProjectSize));
                appendToFile(MessageFormat.format("##allTemplatesInProjectSize {0}\n", allTemplatesInProjectSize), "beforeChange.txt");
                appendToFile(MessageFormat.format("##allTemplatesInProjectSize {0}\n", allTemplatesInProjectSize), "afterChange.txt");

                /* loop over "all Templates In 1 Project" */
                for (Template nextTemplate : allTemplatesInProject) {
                    allPagesInCurrentTemplate = nextTemplate.getAllPages(9999);

                    int allPagesInCurrentTemplateSize = allPagesInCurrentTemplate.size();
                    logger.info(MessageFormat.format("###allPagesInCurrentTemplateSize {0}\n", allPagesInCurrentTemplateSize));

                    /* loop over "all Pages in 1 Template" */
                    for (int pit = 0; pit < allPagesInCurrentTemplateSize; pit++) {
                        Page currentPgInCurrentTemplate = allPagesInCurrentTemplate.get(pit);
                        String currentPgInCurrentTemplatePageId = currentPgInCurrentTemplate.getPageId();

                        if (currentPgInCurrentTemplate.existsInCurrentLanguageVariant()) {
                            logger.info(MessageFormat.format("####PageID in current Template {0}", currentPgInCurrentTemplatePageId));
                            appendToFile(MessageFormat.format("##PageID {0}\n", currentPgInCurrentTemplatePageId), "beforeChange.txt");
                            appendToFile(MessageFormat.format("##PageID {0}\n", currentPgInCurrentTemplatePageId), "afterChange.txt");

                            List<TextElement> filledTextElements = currentPgInCurrentTemplate.getFilledTextElements();

                            /* loop over "filled TextElements in 1 page" */
                            for (TextElement filledElement : filledTextElements) {
                                if (dryRun) {
                                    findAndReplaceAll(filledElement.getName(), filledElement.getText());
                                } else {
                                    filledElement.setText(findAndReplaceAll(filledElement.getName(), filledElement.getText())); //dryrun
                                }

                            } //filledTextElements
                        } //exisit in current lang variant of currentPgInCurrentTemplate
                        else {
                            logger.info(MessageFormat.format("####PageID {0} does not exist in current lang variant", currentPgInCurrentTemplatePageId));
                            appendToFile(MessageFormat.format("##PageID {0} does not exist in current lang variant\n", currentPgInCurrentTemplatePageId), "beforeChange.txt");
                            appendToFile(MessageFormat.format("##PageID {0} does not exist in current lang variant\n", currentPgInCurrentTemplatePageId), "afterChange.txt");
                        }
                    } //pit
                    allPagesInCurrentTemplate.clear();
                } //lang variant
            } //allTemplatesInProject
            /* end of logic */

        } catch (RQLException ex) {
            logger.error(MessageFormat.format("Exception: {0}\n", ex));
            Throwable re = ex.getReason();
            if (re != null) {
                logger.error(MessageFormat.format("Reason: {0}\n Message: {1}\n", re, re.getMessage()));
            }
        } finally {}
        client.disconnect();
        logger.info("End of Java Program");
    }

    private static String findAndReplaceAll(String name, String text) {

        appendToFile(MessageFormat.format("{0}\n {1}\n\n", name, text), "beforeChange.txt");

        text = text.replaceAll("<strong>", "<h2>").replaceAll("<h2><h2>", "<h2><b>");
        text = text.replaceAll("</strong>", "</h2>").replaceAll("</h2></h2>", "</b></h2>");

        appendToFile(MessageFormat.format("{0}\n {1}\n\n", name, text), "afterChange.txt");

        return text;
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
    } //appendToFile
} //end if class
