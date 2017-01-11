package com.hlag.jrql.examples.test;

/**
 *
 */
import com.hlcl.rql.as.CmsClient;
import com.hlcl.rql.as.Container;
import com.hlcl.rql.as.Page;
import com.hlcl.rql.as.PasswordAuthentication;
import com.hlcl.rql.as.Project;
import com.hlcl.rql.as.RQLException;
import com.hlcl.rql.as.Template;
import com.hlcl.rql.util.as.PageArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * @author lejafr
 *
 */
public class BatchProgrammingConnectWithUserAndPassword {

    private HashMap<String, String> AllProjectNamesAndGuid = new HashMap<>();

    /**
     * @param projects
     * @throws RQLException
     */
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

    public static void main(String[] args) throws RQLException {

        CmsClient client = null;
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
        
        try {
            String user = "Admin";
            String pw = "X4anato6s";

            client = new CmsClient(new PasswordAuthentication(user, pw));
            client.changeCurrentProjectByName(projectNameInQ);
            logonGuid = client.getLogonGuid();
            projectGuid = client.getCurrentProjectGuid();
            
            project = client.getProject(sessionKey, projectGuid);
            
            template = project.getTemplateByName(templateFolderName, templateNameInQ);
                  
            /* loop over "allPagesInTemplate" */
            allPagesInTemplate = project.getAllPagesBasedOn(template, 99);
            
            Page firstPage = allPagesInTemplate.get(1);
            
            System.out.println("#firstPageID " + firstPage.getPageId());
           
            /* loop over "pgInQContainerChildPages" if > 0 */
            pgInQContainerChildPages = firstPage.getContainerChildPages(moveFromConNameInQ);
            
            Container moveToCon = firstPage.getContainer(moveToConNameInQ);
            
            /* if size is = 0 goto next "allPagesInTemplate" */
            System.out.println("#pgInQContainerChildPages Size " + pgInQContainerChildPages.size());
            
            /* if size is > 0 loop over "pgInQContainerChildPages" */
            Page firstChildPgOfCon = pgInQContainerChildPages.getPage(0);
            
            /* if tmplName is <> "Accordion" loop "pgInQContainerChildPages" */
            String firstChildPgOfConTmpName = firstChildPgOfCon.getTemplateName();
            
            
           /* if tmplName is = "Accordion" disconnet from link (move)*/
           System.out.println("#isBasedOnTemplate " + firstChildPgOfCon.isBasedOnTemplate(moveTmplToBeBasedOn));
           //firstChildPgOfCon.isBasedOnTemplate(moveTmplToBeBasedOn);
           firstChildPgOfCon.disconnectFromAllMultiLinks();
           
           /* set break to false. meaning the move continues */
           
           /* connect to "moveToConNameInQ" */
           moveToCon.connectToExistingPage(firstChildPgOfCon, true, true); //re-connect logic is correct

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
        /*get the template*/
        
       // project.lo
    client.disconnect();
    }

}
