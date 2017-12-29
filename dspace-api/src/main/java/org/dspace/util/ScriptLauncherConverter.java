/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.util;

import org.dspace.content.Task;
import org.dspace.servicemanager.config.DSpaceConfigurationService;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;

import java.util.LinkedList;
import java.util.List;

public class ScriptLauncherConverter {

    public static Task convertScriptLauncherXMLToTask(Element task){
        Task modelTask =  new Task();
        modelTask.setTaskName(task.getChild("name").getValue());
        modelTask.setDescription(task.getChild("description").getValue());
        return modelTask;
    }

    public static List<Task> getAllScriptLauncherTasks(){
        List<Task> parsedTasks = new LinkedList<>();
        Document document = getConfig();
        Element root = document.getRootElement();
        List<Element> commands = root.getChildren("command");
        for (Element task : commands)
        {
           parsedTasks.add(convertScriptLauncherXMLToTask(task));
        }
        return parsedTasks;
    }

    protected static Document getConfig()
    {
        // Load the launcher configuration file
        String config = new DSpaceConfigurationService().getProperty("dspace.dir") +
                System.getProperty("file.separator") + "config" +
                System.getProperty("file.separator") + "launcher.xml";
        SAXBuilder saxBuilder = new SAXBuilder();
        Document doc = null;
        try
        {
            doc = saxBuilder.build(config);
        }
        catch (Exception e)
        {
            System.err.println("Unable to load the launcher configuration file: [dspace]/config/launcher.xml");
            System.err.println(e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
        return doc;
    }
}
