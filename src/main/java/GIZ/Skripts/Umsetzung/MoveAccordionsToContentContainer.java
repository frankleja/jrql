package GIZ.Skripts.Umsetzung;

import com.hlcl.rql.as.CmsClient;
import com.hlcl.rql.as.Container;
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
public class MoveAccordionsToContentContainer {

    private static Logger logger = Logger.getLogger("ibrahim");

    static CmsClient client = null;
    private static File targetFile;
    private static Properties properties;
    private static String newLine = System.lineSeparator();

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

        String projectNameInQ = "Sandbox2_GIZ_INTERNET";
        String templateNameInQ = "Worldwide Text";
        String templateFolderName = "Masterpages - special";
        String moveFromConNameInQ = "CON_toolbox";
        String moveToConNameInQ = "CON_content";
        String moveTmplToBeBasedOn = "TOOLBOX Content";
        List<LanguageVariant> allLanVariants = new ArrayList();

        Template template;
        PageArrayList allPagesInTemplate;
        PageArrayList pgInQContainerChildPages;

        Project project = null;

        try {
            FileHandler fh = new FileHandler("log.txt");
            Logger.getLogger("ibrahim").addHandler(fh);
        } catch (IOException ex) {
            Logger.getLogger(MoveAccordionsToContentContainer.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SecurityException ex) {
            Logger.getLogger(MoveAccordionsToContentContainer.class.getName()).log(Level.SEVERE, null, ex);
        }

        //load CMS login Credentials from File
        for (String key : properties.stringPropertyNames()) {
            user = key;
            pw = properties.getProperty(key);
        }

        try {
            client = new CmsClient(new PasswordAuthentication(user, pw));
            client.changeCurrentProjectByName(projectNameInQ);
            logonGuid = client.getLogonGuid();
            projectGuid = client.getCurrentProjectGuid();

            project = client.getProject(sessionKey, projectGuid);
            allLanVariants = project.getAllLanguageVariants();

            /* set language variant */
            int allLanVariantsSize = allLanVariants.size();
            for (int alv = 0; alv < allLanVariantsSize; alv++) {
                project.setCurrentLanguageVariant(allLanVariants.get(alv));
                logger.log(Level.INFO, "#set Current Language Variant to {0}", allLanVariants.get(alv).getName());
                System.out.println("\n\n#set Current Language Variant to " + allLanVariants.get(alv).getName());

                template = project.getTemplateByName(templateFolderName, templateNameInQ);

                allPagesInTemplate = project.getAllPagesBasedOn(template, 9999);

                int allPagesInTemplateSize = allPagesInTemplate.size();

                logger.log(Level.INFO, "#allPagesInTemplate size {0}", allPagesInTemplateSize);
                System.out.println("#allPagesInTemplate size " + allPagesInTemplateSize);

                int counter = 0;
                /* loop over "allPagesInTemplate" */
                for (int pit = 0; pit < allPagesInTemplateSize; pit++) {
                    Page pageInTemplate = allPagesInTemplate.get(pit);
                    counter++;
                    String currPageID = pageInTemplate.getPageId();
                    if (pageInTemplate.existsInCurrentLanguageVariant()) {

                        logger.log(Level.INFO, MessageFormat.format("##page counter {0} of {1}", counter, allPagesInTemplateSize));
                        logger.log(Level.INFO, "##Page in Template ID {0} ", currPageID);
                        System.out.println("\n##Pages in Template ID " + currPageID);

                        /*perform checks on page that could cause ERRORS */
                        //errorPreventionChecks(pageInTemplate); //not necessary for this page
                        pgInQContainerChildPages = pageInTemplate.getContainerChildPages(moveFromConNameInQ);
                        Container moveToCon = pageInTemplate.getContainer(moveToConNameInQ);
                        Container mvFromCon = pageInTemplate.getContainer(moveFromConNameInQ);
                        int pgInQContainerChildPagesSize = pgInQContainerChildPages.size();
                        System.out.println("##pgInQContainerChildPages Size " + pgInQContainerChildPagesSize);

                        ArrayList<Page> pgToReconnect = new ArrayList();

                        /* loop over pages in the container BUT from the bottom up */
                        for (int ccp = pgInQContainerChildPagesSize - 1; ccp >= 0; ccp--) {

                            Page ChildPgOfConInWorking = pgInQContainerChildPages.getPage(ccp);
                            if (ChildPgOfConInWorking.existsInCurrentLanguageVariant()) {

                                System.out.println("##isBasedOnTemplate " + ChildPgOfConInWorking.isBasedOnTemplate(moveTmplToBeBasedOn) + " + ID: " + ChildPgOfConInWorking.getPageId());

                                if (!ChildPgOfConInWorking.isBasedOnTemplate(moveTmplToBeBasedOn)) {
                                    break;
                                }

                                pgToReconnect.add(ChildPgOfConInWorking);
                                //firstChildPgOfCon.disconnectFromAllMultiLinks(); //dryRun
                                /*<test>*/
                                mvFromCon.disconnectChild(ChildPgOfConInWorking);
                                 /*</test>*/
                            } // exisit in current lang variant
                            else{
                                System.out.println("##page does not exist in current lang variant°°");
                            }
                        } //ccp

                        /* reconnect pages to new container WHILE maintaining previous order */
                        System.out.println("##pages To Reconnect size " + pgToReconnect.size());
                        for (int i = pgToReconnect.size() - 1; i >= 0; i--) {
                            Page page = pgToReconnect.get(i);
                            System.out.println("##pages To Reconnect ID " + pgToReconnect.get(i).getPageId());
                            if (!errorPreventionChecks(page)) {
                                break;
                            }
                            moveToCon.connectToExistingPage(page, true, true); //dryRun
                        } //i
                        //break; //working with just 1 page for now
                    } // exisit in current lang variant
                } //pit
                allPagesInTemplate.clear();
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
        //Project project = client.getProjectByName(projectNameInQ);

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
        if (pageInTemplate.isInStateReleased()) {
            System.out.println("###page is released ^^");
        }
        if (pageInTemplate.isInStateSavedAsDraft()) {
            System.out.println("###page is in state saved as draft");
        }
        if (pageInTemplate.isInStateSavedAsDraftChanged()) {
            System.out.println("###page is in state saved as Draft changed ");
        }
        if (pageInTemplate.isInStateSavedAsDraftNew()) {
            System.out.println("###page is in state saved as draft new");
        }
        if (pageInTemplate.isInStateWaitingForCorrection()) {
            System.out.println("###page is in state waiting for correction");
        }
        if (pageInTemplate.isInStateWaitingForRelease()) {
            System.out.println("###page is in state waiting for release °°");
            //pageInTemplate.release(); //dont try this
        }
        if (pageInTemplate.isInStateWaitingToBeTranslated()) {
            System.out.println("###page is in state waiting to be translated");
        }
        if (pageInTemplate.isLanguageVariantDependent()) {
            System.out.println("###page is laguage variant dependant ^^");
        }
        if (pageInTemplate.isLanguageVariantIndependent()) {
            System.out.println("###page is laguage variant independant ^^");
        }
        if (pageInTemplate.isUnlinked()) {
            System.out.println("###page is unlinked");
        }
        */
        
        /*if (!pageInTemplate.isLocked()) {
        System.out.println("# page is locked by :"+pageInTemplate.getLockedByUserName()+" ^^");
        //pageInTemplate.switchUserTo(client.getConnectedUser());
        }*/
        //System.out.println("###page is locked by :" + pageInTemplate.getLockedByUserName() + " ^^");
        /*
        if (pageInTemplate.isInRecycleBin()) {
            System.out.println("###page is in RecycleBin... no need to move it°°");
            returnValue = false;
        }*/
        return returnValue;
    }
}
