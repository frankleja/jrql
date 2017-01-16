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
import java.text.MessageFormat;
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
    private static String newLine = System.lineSeparator();
    
    //Logger logger = LogManager.getLogger("ibrahim");
//logger.info("INFO", "foo", "bar");

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

    //private HashMap<String, String> AllProjectNamesAndGuid = new HashMap<>();
    /**
     * @param projects
     * @throws RQLException
     */
    public static void main(String[] args) throws RQLException {
        String user = "";
        String pw = "";

        String logonGuid = "";
        String sessionKey = "";
        String projectGuid = "";

        String projectName = "GIZ Master";
        String templateName = "Worldwide Text";
        String templateFolderName = "Masterpages - special";
        String moveFromConNameInQ = "CON_toolbox";
        String moveToConNameInQ = "CON_content";
        String moveTmplToBeBasedOn = "TOOLBOX Content";
        List<LanguageVariant> allLangVariants = new ArrayList();

        Template currentTemplate;
        PageArrayList allPagesInCurrentTemplate;
        PageArrayList ContainerChildPages;

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
            logonGuid = client.getLogonGuid();
            projectGuid = client.getCurrentProjectGuid();

            project = client.getProject(sessionKey, projectGuid);
            allLangVariants = project.getAllLanguageVariants();

            /* set language variant */
            int allLanVariantsSize = allLangVariants.size();
            for (int alv = 0; alv < allLanVariantsSize; alv++) {
                project.setCurrentLanguageVariant(allLangVariants.get(alv));
                logger.log(Level.INFO, "#set Current Language Variant to {0}", allLangVariants.get(alv).getName());
                System.out.println("\n\n#set Current Language Variant to " + allLangVariants.get(alv).getName());

                currentTemplate = project.getTemplateByName(templateFolderName, templateName);

                allPagesInCurrentTemplate = project.getAllPagesBasedOn(currentTemplate, 9999);

                int allPagesInCurrentTemplateSize = allPagesInCurrentTemplate.size();

                logger.log(Level.INFO, "#allPagesInTemplate size {0}", allPagesInCurrentTemplateSize);
                //System.out.println("#allPagesInTemplate size " + allPagesInCurrentTemplateSize);

                int counter = 0;
                /* loop over "allPagesInCurrentTemplate" */
                for (int pit = 0; pit < allPagesInCurrentTemplateSize; pit++) {
                    Page currentPgInCurrentTemplate = allPagesInCurrentTemplate.get(pit);
                    counter++;
                    String currentPageID = currentPgInCurrentTemplate.getPageId();
                    if (currentPgInCurrentTemplate.existsInCurrentLanguageVariant()) {
 /*
                        logger.log(Level.INFO, MessageFormat.format("##page counter {0} of {1}", counter, allPagesInCurrentTemplateSize));
                        logger.log(Level.INFO, "##Page in Template ID {0} ", currentPageID);
                        System.out.println("\n##Pages in Template ID " + currentPageID);
*/
                        List<TextElement> filledTextElements1 = currentPgInCurrentTemplate.getFilledTextElements();

                        int filledTextElementsSize1 = filledTextElements1.size();
                        //System.out.println("\n###size " + filledTextElementsSize1);
                        for (TextElement string : filledTextElements1) {
                            //logger.log(Level.INFO, 
                              //      MessageFormat.format("\n\nID: {0} \ngetName {1} \ngetText {2}", string.getPage().getPageId(), string.getName(), string.getText()));
                            String str = string.getName(); 
                            str = str.replaceFirst("<strong>", "<h2>");
                            //string.setText(findPayLoadOpen(string.getText(), "<strong>"));
                            System.out.println("\nCharAt-1 "+str.charAt(1));
                            System.out.println("\n"+str);
                            //System.out.println("size " + filledTextElementsSize1 + "string " + string.getName());
                            //break;
                        }
/**
                        //perform checks on page that could cause ERRORS 
                        //errorPreventionChecks(currentPgInCurrentTemplate); //not necessary for this page
                        ContainerChildPages = currentPgInCurrentTemplate.getContainerChildPages(moveFromConNameInQ);
                        //Container moveToCon = currentPgInCurrentTemplate.getContainer(moveToConNameInQ);
                        //Container mvFromCon = currentPgInCurrentTemplate.getContainer(moveFromConNameInQ);
                        int ContainerChildPagesSize = ContainerChildPages.size();
                        System.out.println("##pgInQContainerChildPages Size " + ContainerChildPagesSize);

                        // loop over pages in the container BUT from the bottom up 
                        for (int ccp = ContainerChildPagesSize - 1; ccp >= 0; ccp--) {

                            Page pageInContainerChildPages = ContainerChildPages.getPage(ccp);
                            if (pageInContainerChildPages.existsInCurrentLanguageVariant()) {

                                //do what u want
                                List<TextElement> filledTextElements = pageInContainerChildPages.getFilledTextElements();

                                int filledTextElementsSize = filledTextElements.size();
                                System.out.println("\n###size " + filledTextElementsSize);
                                for (TextElement string : filledTextElements) {
                                    logger.log(Level.INFO, MessageFormat.format("getName {0} getText {1}", string.getName(), string.getText()));
                                    //string.getName(); 
                                    //string.getText();
                                    System.out.println("size " + filledTextElementsSize + "string " + string.getName());
                                    //break;
                                }

                            } // exisit in current lang variant of pageInContainerChildPages
                            else {
                                System.out.println("##page does not exist in current lang variant°°");
                            }
                        } //ccp
**/
                        //break; //working with just 1 page for now
                    } // exisit in current lang variant of currentPgInCurrentTemplate
                    //break; //working with just 1 lang variant for now
                } //pit
                allPagesInCurrentTemplate.clear();
            } //lang variant
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

        //listAllProjectNames(client.getAllProjects());
        //listAllTEmplateNames(client.getProject(sessionKey, projectGuid)); //works
        client.disconnect();
        logger.info("End of Java Program");
    }

    public static void listAllProjectNames(List projects) throws RQLException {
        //List projects = client.getAllProjects();
        try {
            for (int i = 0; i < projects.size(); i++) {
                Project projectList = (Project) projects.get(i);
                String projectName = projectList.getName();
                String projectGuid = projectList.getProjectGuid();
                System.out.println("# projectName " + projectName + " " + projectGuid);
            }
        } catch (RQLException rQLException) {
        }
    }

    public static void listAllTEmplateNames(Project project) throws RQLException {
        //Project project = client.getProjectByName(projectName);

        try {
            List templates = project.getAllTemplates();
            for (int i = 0; i < templates.size(); i++) {
                Template templateList = (Template) templates.get(i);
                String templateName = templateList.getName();
                String templateGuid = templateList.getTemplateGuid();
                System.out.println("# templateName " + templateName + " " + templateGuid);
            }
        } catch (RQLException rQLException) {
        }
    }

    private static boolean errorPreventionChecks(Page pageInTemplate) throws RQLException {

        boolean returnValue = true;

        /*
        if (currentPgInCurrentTemplate.isInStateReleased()) {
            System.out.println("###page is released ^^");
        }
        if (currentPgInCurrentTemplate.isInStateSavedAsDraft()) {
            System.out.println("###page is in state saved as draft");
        }
        if (currentPgInCurrentTemplate.isInStateSavedAsDraftChanged()) {
            System.out.println("###page is in state saved as Draft changed ");
        }
        if (currentPgInCurrentTemplate.isInStateSavedAsDraftNew()) {
            System.out.println("###page is in state saved as draft new");
        }
        if (currentPgInCurrentTemplate.isInStateWaitingForCorrection()) {
            System.out.println("###page is in state waiting for correction");
        }
        if (currentPgInCurrentTemplate.isInStateWaitingForRelease()) {
            System.out.println("###page is in state waiting for release °°");
            //pageInTemplate.release(); //dont try this
        }
        if (currentPgInCurrentTemplate.isInStateWaitingToBeTranslated()) {
            System.out.println("###page is in state waiting to be translated");
        }
        if (currentPgInCurrentTemplate.isLanguageVariantDependent()) {
            System.out.println("###page is laguage variant dependant ^^");
        }
        if (currentPgInCurrentTemplate.isLanguageVariantIndependent()) {
            System.out.println("###page is laguage variant independant ^^");
        }
        if (currentPgInCurrentTemplate.isUnlinked()) {
            System.out.println("###page is unlinked");
        }
         */
 /*if (!currentPgInCurrentTemplate.isLocked()) {
        System.out.println("# page is locked by :"+currentPgInCurrentTemplate.getLockedByUserName()+" ^^");
        //pageInTemplate.switchUserTo(client.getConnectedUser());
        }*/
        //System.out.println("###page is locked by :" + currentPgInCurrentTemplate.getLockedByUserName() + " ^^");
        /*
        if (currentPgInCurrentTemplate.isInRecycleBin()) {
            System.out.println("###page is in RecycleBin... no need to move it°°");
            returnValue = false;
        
        label: for (int i = 0; i < x; i++) {
    for (int j = 0; j < i; j++) {
        if (something(i, j)) break label; // jumps out of the i loop
    }
} 
// i.e. jumps to here
        }*/
        return returnValue;
    }

    private static String findPayLoadOpen(String text, String strong) {
    
        System.out.println("CharAt-1 "+text.charAt(-1));
    
    
    text = text.replaceAll(text, strong);
    return text;
    }
}
