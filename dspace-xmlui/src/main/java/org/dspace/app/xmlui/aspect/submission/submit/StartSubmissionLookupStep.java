/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */

package org.dspace.app.xmlui.aspect.submission.submit;

import org.dspace.app.xmlui.aspect.submission.AbstractSubmissionStep;
import org.dspace.app.xmlui.utils.UIException;
import org.dspace.app.xmlui.wing.Message;
import org.dspace.app.xmlui.wing.WingException;
import org.dspace.app.xmlui.wing.element.*;
import org.dspace.authorize.AuthorizeException;
import org.dspace.content.Collection;
import org.dspace.content.MetadataValue;
import org.dspace.content.factory.ContentServiceFactory;
import org.dspace.content.service.ItemService;
import org.dspace.content.service.MetadataFieldService;
import org.dspace.importer.external.service.ImportService;
import org.dspace.submit.AbstractProcessingStep;
import org.dspace.utils.DSpace;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.sql.SQLException;

/**
 * Created by jonas - jonas@atmire.com on 06/11/15.
 */
public class StartSubmissionLookupStep extends AbstractSubmissionStep {

    protected static final Message T_title =
            message("xmlui.Submission.submit.StartSubmissionLookupStep.title");
    protected static final Message T_lookup_help =
            message("xmlui.Submission.submit.StartSubmissionLookupStep.lookup_help");
    protected static final Message T_submit_lookup =
            message("xmlui.Submission.submit.StartSubmissionLookupStep.submit_lookup");
    protected static final Message T_next_import =
            message("xmlui.Submission.general.submission.next_import");
    protected static final Message T_next_no_import =
            message("xmlui.Submission.general.submission.next_no_import");
    protected static final Message T_complete_import =
            message("xmlui.Submission.general.submission.complete_import");
    protected static final Message T_complete_no_import =
            message("xmlui.Submission.general.submission.complete_no_import");

//    private ItemService itemService = ContentServiceFactory.getInstance().getItemService();
//    MetadataFieldService metadataFieldService = ContentServiceFactory.getInstance().getMetadataFieldService();

    @Override
    public List addReviewSection(List reviewList) throws SAXException, WingException, UIException, SQLException, IOException, AuthorizeException {
        return null;
    }

    public void addPageMeta(PageMeta pageMeta) throws SAXException,
            WingException
    {

        pageMeta.addMetadata("title").addContent(T_submission_title);
        pageMeta.addTrailLink(contextPath + "/",T_dspace_home);
        pageMeta.addTrail().addContent(T_submission_trail);

        pageMeta.addMetadata("javascript", null, "handlebars", true).addContent("../../static/handlebars/handlebars.js");
        pageMeta.addMetadata("javascript", null, "helpers", true).addContent("../../static/handlebars/helpers.js");
        pageMeta.addMetadata("javascript", null, "submission-lookup", true).addContent("../../static/js/submission-lookup.js");
        pageMeta.addMetadata("stylesheet", "screen", "datatables", true).addContent("../../static/Datatables/DataTables-1.8.0/media/css/datatables.css");
        pageMeta.addMetadata("javascript", "static", "datatables", true).addContent("static/Datatables/DataTables-1.8.0/media/js/jquery.dataTables.min.js");
    }
    public void addBody(Body body) throws SAXException, WingException,
            UIException, SQLException, IOException, AuthorizeException
    {
        Collection collection = submission.getCollection();
        String actionURL = contextPath + "/handle/"+collection.getHandle() + "/submit/" + knot.getId() + ".continue";
        Division div = body.addInteractiveDivision("StartSubmissionLookupStep",actionURL,Division.METHOD_POST,"primary submission");
        div.setHead(T_submission_head);
        addSubmissionProgressList(div);

        List form = div.addList("submit-lookup",List.TYPE_FORM);

        form.setHead(T_title);

        form.addItem().addContent(T_lookup_help);

        Item item = form.addItem("lookup-group", "input-group");
        Select select = item.addSelect("type"/*, "form-control"*/);

        ImportService importService = new DSpace().getServiceManager().getServiceByName("importService", ImportService.class);

        for(String importUrl : importService.getImportUrls()) {
            select.addOption(importUrl, message("xmlui.Submission.submit.LookupStep." + importUrl));
        }
        item.addText("search"/*, "form-control"*/);
        item.addButton("lookup"/*, "btn btn-secondary"*/).setValue(T_submit_lookup);


        div.addDivision("lookup-modal");

        addControlButtons(form);
    }

    /**
     * Adds the "<-Previous", "Save/Cancel" and "Next->" buttons
     * to a given form.  This method ensures that the same
     * default control/paging buttons appear on each submission page.
     * <P>
     * Note: A given step may define its own buttons as necessary,
     * and not call this method (since it must be explicitly invoked by
     * the step's addBody() method)
     *
     * @param controls
     *          The List which will contain all control buttons
     */
    @Override
    public void addControlButtons(List controls)
            throws WingException
    {
        Item actions = controls.addItem();

        // only have "<-Previous" button if not first step
        if(!isFirstStep())
        {
            actions.addButton(AbstractProcessingStep.PREVIOUS_BUTTON).setValue(T_previous);
        }

        // always show "Save/Cancel"
        actions.addButton(AbstractProcessingStep.CANCEL_BUTTON).setValue(T_save);

        // If last step, show "Complete Submission"
        if(isLastStep())
        {
            actions.addButton(AbstractProcessingStep.NEXT_BUTTON).setValue(T_complete_import);
            actions.addButton(AbstractProcessingStep.NEXT_BUTTON).setValue(T_complete_no_import);
        }
        else // otherwise, show "Next->"
        {
            actions.addButton(AbstractProcessingStep.NEXT_BUTTON, "hidden").setValue(T_next_import);
            actions.addButton(org.dspace.submit.step.XMLUIStartSubmissionLookupStep.NEXT_NO_IMPORT_BUTTON).setValue(T_next_no_import);
        }
    }

}
