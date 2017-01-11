package com.hlcl.rql.util.as;

import java.util.List;

import com.hlcl.rql.as.CmsClient;
import com.hlcl.rql.as.ContentElement;
import com.hlcl.rql.as.LanguageVariant;
import com.hlcl.rql.as.Page;
import com.hlcl.rql.as.Project;
import com.hlcl.rql.as.RQLException;
import com.hlcl.rql.as.TemplateElement;

/**
 * Test of the language variant dependent values matrix class.
 */
public class LanguageVariantDependentValuesMatrixTest {

	/**
	 * start application
	 */
	public static void main(String[] args) {
		try {
			long start = System.currentTimeMillis();

			String logonGuid="A072654BF5EE4499BF94EBCFBD7DF76F";
			String sessionKey="35DAD9FD229146C0BF0CCF365661E2A4";
			String projectGuid="73671509FA5C43ED8FC4171AD0298AD2";

			// input values
			String[] languages = { "en", "zh", "es", "de" };

			CmsClient client = new CmsClient(logonGuid);
			Project project = client.getProject(sessionKey, projectGuid);

			// collect all lv values for all lvs and restore lv afterwards
			// remember current lv for later restore
			LanguageVariant currentLv = project.getCurrentLanguageVariant();
			LanguageVariantDependentValuesMatrix matrix = new LanguageVariantDependentValuesMatrix();
			try {
				for (int i = 0; i < languages.length; i++) {
					// switch language variant
					String language = languages[i];
					LanguageVariant lv = project.setCurrentLanguageVariantByRfcLanguageId(language);
					System.out.println(project.getCurrentLanguageVariant().getName());

					// get page content elements in current lv
					Page currentPg = project.getPageById("3466");
					System.out.println("dbg " + lv.getName() + "=" + currentPg.getHeadline());
					matrix.addProperty("headline", currentPg.getHeadline(), lv);
					matrix.addProperty("changed by", currentPg.getLastChangedByUserName(), lv);
					matrix.addProperty("changed on", currentPg.getLastChangedOnAsddMMyyyy(), lv);
					matrix.addProperty("state", currentPg.getStateInfo(), lv);

					List<ContentElement> contentElements = currentPg.getContentElements(false);

					// // out values
					// for (ContentElement contentElement : contentElements) {
					// System.out.println("dbg " + contentElement.getTemplateElementName() + " " + contentElement.getValueAsString());
					// }
					matrix.add(contentElements, lv);
				}

				// out properties
				String[] properties = { "headline", "changed by", "changed on", "state" };
				for (int j = 0; j < properties.length; j++) {
					String property = properties[j];

					System.out.print(property + " ");
					for (int i = 0; i < languages.length; i++) {
						// switch language variant
						String language = languages[i];
						LanguageVariant languageVariant = project.getLanguageVariantByRfcLanguageId(language);
						System.out.print(languageVariant.getRfcLanguageId() + "="
								+ matrix.getPropertyValue(property, languageVariant) + " ");
					}
					System.out.println();
				}

				// out content element values per lv
				List<TemplateElement> templateElements = matrix.getTemplateElements();
				System.out.println(templateElements.size() + " content elements for page found");
				// Set<LanguageVariant> languageVariants = matrix.getLanguageVariants();
				LanguageVariant mainLv = project.getMainLanguageVariant();
				for (TemplateElement templateElement : templateElements) {
					System.out.print(templateElement.isLanguageVariantDependent() + " " + templateElement.getName() + " ");
					for (int i = 0; i < languages.length; i++) {
						// switch language variant
						String language = languages[i];
						LanguageVariant languageVariant = project.getLanguageVariantByRfcLanguageId(language);
						ContentElement contentElement = matrix.getContentElement(templateElement, languageVariant);
						String value = "n/a";
						if (contentElement != null) {
							value = contentElement.getValueAsString();
						}
						System.out.print("result " + languageVariant.getRfcLanguageId() + "=" + value + " ");
						if (matrix.isTextValueDifferentFromMainLanguage(templateElement, mainLv, languageVariant)) {
							System.out.println("  ==> value different");
						}
					}
					System.out.println();
				}
			} finally {
				project.setCurrentLanguageVariant(currentLv);
			}

			// display duration
			long end = System.currentTimeMillis();
			System.out.println("Duration=" + (end - start));

		} catch (RQLException ex) {
			ex.printStackTrace();
			System.out.print(ex.getMessage());

			Throwable re = ex.getReason();
			if (re != null) {
				re.printStackTrace();
				System.out.print(re.getMessage());
			}
		}
	}
}
