/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.app.xmlui.aspect.submission.submit;

import java.util.Map;

import org.apache.avalon.framework.parameters.Parameters;
import org.apache.cocoon.acting.AbstractAction;
import org.apache.cocoon.environment.ObjectModelHelper;
import org.apache.cocoon.environment.Redirector;
import org.apache.cocoon.environment.Request;
import org.apache.cocoon.environment.SourceResolver;
import org.apache.log4j.*;
import org.dspace.app.xmlui.utils.ContextUtil;
import org.dspace.content.WorkspaceItem;
import org.dspace.content.factory.ContentServiceFactory;
import org.dspace.content.service.WorkspaceItemService;
import org.dspace.core.Context;

/**
 * Remove all selected submissions. This action is used by the 
 * submission page, the user may check each unfinished submission 
 * and when he clicks the remove submissions button this action 
 * will remove them all. 
 *
 * @author Scott Phillips
 */
public class RemoveSubmissionsAction extends AbstractAction
{
	protected WorkspaceItemService workspaceItemService = ContentServiceFactory.getInstance().getWorkspaceItemService();

	private static final Logger log = Logger.getLogger(RemoveSubmissionsAction.class);

	/**
	 * Remove all selected submissions
	 *
	 * @param redirector
	 * @param resolver
	 * @param objectModel
	 *            Cocoon's object model
	 * @param source
	 * @param parameters
	 */
	public Map act(Redirector redirector, SourceResolver resolver, Map objectModel,
				   String source, Parameters parameters) throws Exception
	{

		Context context = ContextUtil.obtainContext(objectModel);
		Request request = ObjectModelHelper.getRequest(objectModel);


		String[] workspaceIDs = request.getParameterValues("workspaceID");

		if (workspaceIDs != null)
		{
			for (String workspaceID : workspaceIDs)
			{
				// If they selected to remove the item then delete everything.
				WorkspaceItem workspaceItem = workspaceItemService.find(context, Integer.valueOf(workspaceID));
				try{
					//commit all changes that happened before this delete call.
					context.dispatchEvents();
					workspaceItemService.deleteAll(context, workspaceItem);
				}
				catch (Exception e){
					// rollback the delete on error
					context.rollBack();
					log.error(e.getMessage(),e);
				}
			}
		}

		return null;
	}

}
