package GIZ.Skripts.Umsetzung;

import com.hlcl.rql.as.CmsClient;
import com.hlcl.rql.as.Container;
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

/**
 * @author Ibrahim Sawadogo
 *
 */
public class MoveAccordionsToContentContainer {

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

        String projectNameInQ = "GIZ Master";
        String templateNameInQ = "Worldwide Text";
        String templateFolderName = "Masterpages - special";
        String moveFromConNameInQ = "CON_toolbox";
        String moveToConNameInQ = "CON_content";
        String moveTmplToBeBasedOn = "TOOLBOX Content";

        Template template;
        PageArrayList allPagesInTemplate;
        PageArrayList pgInQContainerChildPages;

        Project project = null;

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

            template = project.getTemplateByName(templateFolderName, templateNameInQ);

            allPagesInTemplate = project.getAllPagesBasedOn(template, 99);
            System.out.println("#allPagesInTemplate size " + allPagesInTemplate.size());

            /* loop over "allPagesInTemplate" */
            for (int pit = 0; pit < allPagesInTemplate.size(); pit++) {
                Page pageInTemplate = allPagesInTemplate.get(pit);

                System.out.println("\n##Pages in Template ID " + pageInTemplate.getPageId());
                /*perform checks on page that could cause ERRORS */
                errorPreventionChecks(pageInTemplate);

                pgInQContainerChildPages = pageInTemplate.getContainerChildPages(moveFromConNameInQ);
                Container moveToCon = pageInTemplate.getContainer(moveToConNameInQ);

                System.out.println("##pgInQContainerChildPages Size " + pgInQContainerChildPages.size());

                ArrayList<Page> pgToReconnect = new ArrayList();

                /* loop over pages in the container BUT from the bottom up */
                for (int ccp = pgInQContainerChildPages.size() - 1; ccp >= 0; ccp--) {

                    Page firstChildPgOfCon = pgInQContainerChildPages.getPage(ccp);
                    System.out.println("##isBasedOnTemplate " + firstChildPgOfCon.isBasedOnTemplate(moveTmplToBeBasedOn) + " + ID: " + firstChildPgOfCon.getPageId());

                    if (!firstChildPgOfCon.isBasedOnTemplate(moveTmplToBeBasedOn)) {
                        break;
                    }

                    pgToReconnect.add(firstChildPgOfCon);
                    //firstChildPgOfCon.disconnectFromAllMultiLinks(); //dryRun
                } //ccp

                /* reconnect pages to new container WHILE maintaining previous order */
                System.out.println("##pages To Reconnect size " + pgToReconnect.size());
                for (int i = pgToReconnect.size() - 1; i >= 0; i--) {
                    Page page = pgToReconnect.get(i);
                    System.out.println("##pages To Reconnect ID " + pgToReconnect.get(i).getPageId());
                    errorPreventionChecks(page);
                    //moveToCon.connectToExistingPage(page, true, true); //dryRun
                } //i
                //break; //working with just 1 page for now
            } //pit
            /* end of logic */

        } catch (RQLException ex) {
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

    private static void errorPreventionChecks(Page pageInTemplate) throws RQLException {

        if (pageInTemplate.isInRecycleBin()) {
            System.out.println("###page is in RecycleBin");
        }
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
            System.out.println("###page is in state waiting for release");
            pageInTemplate.release();
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
        /*if (!pageInTemplate.isLocked()) {
        System.out.println("# page is locked by :"+pageInTemplate.getLockedByUserName()+" ^^");
        //pageInTemplate.switchUserTo(client.getConnectedUser());
        }*/
        System.out.println("###page is locked by :" + pageInTemplate.getLockedByUserName() + " ^^");
    }
}
