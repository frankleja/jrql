package GIZ.Skripts.TextAkkordeons;

import com.hlcl.rql.as.CmsClient;
import com.hlcl.rql.as.LanguageVariant;
import com.hlcl.rql.as.Page;
import com.hlcl.rql.as.PasswordAuthentication;
import com.hlcl.rql.as.Project;
import com.hlcl.rql.as.RQLException;
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
 * @author Ibrahim Sawadogo (http://IbrahimSawadogo.pro)
 *
 * The purpose of this script is to loop through a given project, all pages
 * based on a specific template and fetch all filled text. Then again go through
 * this text and "delete all its content".
 *
 */
public class delTextFromAccordions {

    private static final Logger logger = Logger.getLogger(delTextFromAccordions.class);

    private static CmsClient client = null;
    private static File targetFile;
    private static Properties properties;

    static boolean logIt = true;
    static boolean dryRun = true;

    static String beforeFilename = "dtaB4file.txt";
    static String afterFilename = "dtaAfile.txt";

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
        String projectName = "GIZ Master";
        String accordionTemplateName = "ACCORDION text";
        String templateFolderName = "Multi-applicable Modules";

        List<LanguageVariant> allLangVariants = new ArrayList();
        Project project = null;

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

                String CurrentLanguageVariantName = allLangVariants.get(alv).getName();
                System.out.println("\n\n#Language Variant " + CurrentLanguageVariantName);
                if (logIt) {
                    appendToFile(MessageFormat.format("##Language Variant {0}\n", CurrentLanguageVariantName), beforeFilename);
                    appendToFile(MessageFormat.format("##Language Variant {0}\n", CurrentLanguageVariantName), afterFilename);
                }

                PageArrayList allAccordionPages = project.getAllPagesBasedOn(templateFolderName, accordionTemplateName, 9999);

                int allAccordionPagesSize = allAccordionPages.size();
                logger.info(MessageFormat.format("##allAccordionPagesSize {0}", allAccordionPagesSize));
                if (logIt) {
                    appendToFile(MessageFormat.format("##allAccordionPagesSize {0}\n", allAccordionPagesSize), beforeFilename);
                    appendToFile(MessageFormat.format("##allAccordionPagesSize {0}\n", allAccordionPagesSize), afterFilename);
                }

                /* loop over "all Accordion Pages" */
                for (Page nextPage : allAccordionPages) {

                    String nextPageID = nextPage.getPageId();

                    if (nextPage.existsInCurrentLanguageVariant()) {
                        logger.info(MessageFormat.format("####PageID in current Template {0}", nextPageID));
                        if (logIt) {
                            appendToFile(MessageFormat.format("##PageID {0}\n", nextPageID), beforeFilename);
                            appendToFile(MessageFormat.format("##PageID {0}\n", nextPageID), afterFilename);
                        }

                        List<TextElement> filledTextElements = nextPage.getFilledTextElements();

                        /* loop over "filled TextElements in 1 page" */
                        for (TextElement filledElement : filledTextElements) {
                            if (!dryRun) {
                                //filledElement.setText(modifyFilledText(filledElement.getName(), filledElement.getText())); //dryrun
                            } else {
                                modifyFilledText(filledElement.getName(), filledElement.getText());
                            } // dryRun if
                        } //filledTextElements
                    } //exisit in current lang variant of currentPgInCurrentTemplate
                    else {
                        logger.info(MessageFormat.format("####PageID {0} does not exist in current lang variant", nextPageID));
                        if (logIt) {
                            appendToFile(MessageFormat.format("##PageID {0} does not exist in current lang variant\n", nextPageID), beforeFilename);
                            appendToFile(MessageFormat.format("##PageID {0} does not exist in current lang variant\n", nextPageID), afterFilename);
                        }
                    } //else
                } //lang variant
            } //allTemplatesInProject
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

    private static String modifyFilledText(String name, String text) {

        if (logIt) {
            appendToFile(MessageFormat.format("{0}\n {1}\n\n", name, text), beforeFilename);
        }
        text = "";
        if (logIt) {
            appendToFile(MessageFormat.format("{0}\n {1}\n\n", name, text), afterFilename);
        }
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
