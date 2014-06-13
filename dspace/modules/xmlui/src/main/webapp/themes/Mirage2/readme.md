# Introduction #

Mirage 2 is a responsive XMLUI theme for DSpace 4. It is based on the responsive theme for the [Open Knowledge Repository](https://openknowledge.worldbank.org/) by The World Bank. 

Like the original Mirage theme, we started from [HTML5 boilerplate](http://h5bp.com) for the basic structure of each page and best practices with regards to cross browser compatibility, accessibility and performance.

We used [Bootstrap 3](http://getbootstrap.com/) to have a sturdy responsive grid to base everything on. We chose the SASS version because that allows us to use it in conjunction with the [Compass](http://compass-style.org/) framework, which provides a great library of SASS classes and mixins. Compass also comes with a custom version of the SASS preprocessor we can use to concatenate and minify all stylesheets. We also included the [Handlebars](http://handlebarsjs.com/) templating framework to create more robust Javascript views.

All these extra dependencies made us reconsider the way front-end dependencies were managed in Mirage 2. A common problem with themes based on the first Mirage was that its dependencies (jQuery, Modernizr, …) were outdated. The versions that came with the theme kept on being used because no-one remembered to update them when creating a new Mirage based theme. So we decided to no longer include 3d party dependencies with the theme and instead go for a combination of [Bower](http://bower.io/) and [Grunt](http://gruntjs.com/) to fetch and combine dependencies at build time.

These new technologies are put in place to make it easier for the theme developer to produce great themes, and we encourage every one to at least have a stab at using them. Once you've seen the power of variables and functions in SASS you'll wonder how you ever got on without them. However we understand if you don't want to install a bunch of new tools just to make a few minor changes to the theme. So we've also created a pre-compiled version of Mirage 2. This version doesn't have any extra dependencies and is no harder to install and to work with than any other DSpace theme.

# The Pre-compiled Version #

If you don't want to run through the process of building Mirage 2 from scratch you can choose to use the pre-compiled version. This version can be installed in the same way as any other DSpace theme. Keep in mind that it can be harder to customise since a lot of CSS and JavaScript files are minified and combined in a single file. 

## Installation ##

Unpack the theme to your *src/dspace/modules/xmlui/src/main/webapp/themes* folder.

Add the following to the *<themes>* section of  *src/dspace/config/xmlui.xconf*, replacing the currently active theme.

```xml
    <theme name="Mirage 2" regex=".*" path="Mirage2_precompiled/" /> 
```

## Styles ##

As with the normal Mirage 2 you can switch between the two color schemes. A classic Mirage color scheme or the standard Bootstrap color scheme. By default the classic Mirage color scheme is used but you can switch to the different scheme in *Mirage2/xsl/core/page-structure.xsl*.

```html
    <!--### CLASSIC MIRAGE COLOR SCHEME START ###-->
    <link rel="stylesheet" href="{concat($theme-path, 'styles/bootstrap-classic-mirage-colors-min.css')}"/>
    <link rel="stylesheet" href="{concat($theme-path, 'styles/classic-mirage-style.css')}"/>
    <!--### CLASSIC MIRAGE COLOR SCHEME END ###-->

    <!--### BOOTSTRAP COLOR SCHEME START ###-->
    <!--<link rel="stylesheet" href="{concat($theme-path, 'styles/bootstrap-min.css')}"/>-->
    <!--### BOOTSTRAP COLOR SCHEME END ###-->

    <link rel="stylesheet" href="{concat($theme-path, 'styles/dspace-bootstrap-tweaks.css')}"/>
    <link rel="stylesheet" href="{concat($theme-path, 'styles/jquery-ui-1.10.3.custom.css')}"/>
```

To enable the classic Mirage color scheme, uncomment all link tags between *CLASSIC MIRAGE COLOR SCHEME START* and *CLASSIC MIRAGE COLOR SCHEME END*. Be sure to comment all link tags between *BOOTSTRAP COLOR SCHEME START* and *BOOTSTRAP COLOR SCHEME END*. Do the opposite to use the Bootstrap color scheme.

Both schemes need *Mirage2/styles/dspace-bootstrap-tweaks.css* and *Mirage2/styles/jquery-ui-1.10.3.custom.css*. *Mirage2/styles/dspace-bootstrap-tweaks.css* are the minimal required styles you need on top of Bootstrap to display everything in a proper way. Those styles are mostly related to the sidebar behavior. As the name reveals, *Mirage2/styles/jquery-ui-1.10.3.custom.css* are JQuery UI related styles. jQuery UI is included for it's calendar and autocomplete widgets. The calendar widget is only used as fallback for browsers that don't have the HTML 5 calendar input field.

On top of that the classic Mirage color scheme needs *Mirage2/styles/bootstrap-classic-mirage-colors-min.css*, this is the original Bootstrap compiled with custom colors and *Mirage2/styles/classic-mirage-style.css*, which contains all additional styles.

The Bootstrap color scheme only needs one additional CSS file, *Mirage2/styles/bootstrap-min.css*. This is almost the default Bootstrap CSS, only font-paths are modified. 

If you want to base your theme on an existing Bootstrap theme (like the ones at [bootswatch.com](http://bootswatch.com)) you can do so by using the standard Bootstrap style and replacing the reference to *Mirage2/styles/bootstrap-min.css* in *page-structure.xsl* with a reference to the css file(s) of your bootstrap theme of choice. 

## JavaScript ##

All JavaScript code that comes with the theme is combined and minified in a single file, *Mirage2/scripts/theme.js*. In case you need to modify this code it is recommended to replace it with the original JavaScript files from the source version and start working from those. Note that the *theme.js* file also contains all the required third party javascript dependencies like jQuery and the Bootstrap javascript components.

## DRI Preprocessing ##

The [DRI Preproccessing section](#dri-pre-processing) in the "Source Version" section, is also relevant for the pre-compiled version
# The Source Version #

If you're willing to invest a little more effort in to creating your Mirage 2 based DSpace theme, the source version is the way to go. It puts a lot more tools at your disposal to help you solve common problems quickly and adhere to best practices easily.

## Installation ##

### Prerequisites for OSX / Linux ###

All extra tools in the Mirage 2 build process run on either Node js or Ruby, so you'll need both to be able to build the theme. The instructions below will cover the install of Node and Ruby using their respective version managers.

#### Node ####

We recommend using [nvm](https://github.com/creationix/nvm)  (Node Version Manager), because it makes it easy to install, use and upgrade node without super user rights.

First download and install nvm:

```bash
    curl https://raw.githubusercontent.com/creationix/nvm/v0.5.1/install.sh | sh 
```

Then, close and reopen your terminal, and install a node version. We’ve been using 0.10.17 during the development of the theme, but it may very well work on other versions

```bash
    nvm install 0.10.17 
```

Set the node version you installed as the default version.

```bash
    nvm alias default 0.10.17
```


#### Bower ####

You can install bower using the node package manager. The *-g* means install it globally, not as part of a specific project.

```bash
    npm install -g bower
```
Afterwards the command *bower* should show a help message.

#### Grunt ####

Grunt should also be installed globally using the node package manager:

```bash
    npm install -g grunt && npm install -g grunt-cli 
```

Afterwards the command *grunt --version* should show the grunt-cli version number

#### Ruby ####

For the same reasons as with Node, we’d advise using ruby via [RVM](http://rvm.io/)  (Ruby Version Manager). Even on OS X, which comes with a version of ruby preinstalled, you can save yourself a lot of hassle by using RVM instead. (In most cases there is no need to uninstall the system ruby first). Note that **you need sudo rights to perform the RVM installation**. You won't need sudo again to use RVM, ruby or gem later on


#### Note: ####
On OS X you need to have the XCode command line tools installed. To test if they're installed try to type *make* in your terminal, if it says “command not found” follow [these instructions](http://www.computersnyou.com/2025/2013/06/install-command-line-tools-in-osx-10-9-mavericks-how-to/) to install them  
(If this should fail, then you can find and download the Xcode command line tools directly from [Apple Developer site](https://developer.apple.com/downloads/index.action) - You will need an Apple id for this)

Install RVM and ruby:

```bash
    curl -sSL https://get.rvm.io | bash -s stable --ruby
```

#### Compass ####

Compass is built on the SASS CSS preprocessor, and at the time of writing there is an issue with the latest version of SASS (v3.3), so first you'll have to install the last known stable version using ruby's package manager:

```bash
    gem install sass -v 3.2.10
```

Then you can install compass:

```bash
    gem install compass
```

Afterwards the command *compass* should show a help message.

### Prerequisites for Windows ###

All extra tools in the Mirage 2 build process run on either Node js or Ruby, so you'll need both to be able to build the theme. The instructions below will cover the install of Node and Ruby.

#### Git ####

Download and install Git for Windows: [Git](http://git-scm.com/)

#### Node ####

Download and install NodeJs: [NodeJS](http://nodejs.org/)

#### Bower ####

You can install bower using the node package manager. The *-g* means install it globally, not as part of a specific project.

Execute following command in Windows command prompt:
(Open a Windows command prompt by pressing cmd-R, then type 'cmd' and enter.)

```bash
    npm install -g bower
```
Afterwards the command *bower* should show a help message.

#### Grunt ####

Grunt should also be installed globally using the node package manager:

Perform the following in a Windows command prompt:

```bash
    npm install -g grunt && npm install -g grunt-cli 
```

Afterwards the command *grunt --version* should show the grunt-cli version number

#### Ruby ####
Download and install: [Ruby Installer](http://rubyinstaller.org/)

Make sure its environment variables are set in system variables

Open computer properties:

![Step 1](http://gitlab.atmire.com/contributions/mirage-2/raw/master/images/documentation/sysvars1.png)


Open "advanced sytem settings". Open "Advanced" tab, and click "environment variables":

![Step 2](http://gitlab.atmire.com/contributions/mirage-2/raw/master/images/documentation/sysvars2.png)



Add new variables *GEM_HOME* and *GEM_PATH* pointing to your Ruby gems directory.

#### Compass ####

Compass is built on the SASS CSS preprocessor, and at the time of writing there is an issue with the latest version of SASS (v3.3), so first you'll have to install the last known stable version using ruby's package manager:

Perform the following in a Windows command prompt:

```bash
    gem install sass -v 3.2.10
```

Then you can install compass:

```bash
    gem install compass
```

Afterwards the command *compass* should show a help message.

### The Theme ###

Unpack the theme to your *src/dspace/modules/xmlui/src/main/webapp/themes* folder.

Add the following to the *<themes>* section of  *src/dspace/config/xmlui.xconf*, replacing the currently active theme.

```xml
    <theme name="Mirage 2" regex=".*" path="Mirage2/" /> 
```

### xmlui/pom.xml ###

Downloading the resources, preprocessing, concatenating and minifying is done by bower and grunt, and they are in turn triggered by maven. 

The maven build process needs to be altered to include grunt, so you’ll have to modify *src/dspace/modules/xmlui/pom.xml*. The file *example_xmlui_pom.xml* in the Mirage2 directory is a DSpace 4 xmlui pom.xml file with the necessary changes already applied to it. So you can use that file, or follow the changes below to modify your own version.

First add the following properties to */project/properties*

```xml
    <grunt.environment>prod</grunt.environment>
    <grunt.color.scheme>classic_mirage_style</grunt.color.scheme>  
```

Then add the following to */project/build/plugins*:

```xml
    <plugin>
        <groupId>org.apache.maven.plugins</groupId>
        <artifactId>maven-resources-plugin</artifactId>
        <executions>
            <execution>
                <goals>
                    <goal>copy-resources</goal>
                </goals>
                <phase>process-resources</phase>
                <configuration>
                    <outputDirectory>${project.build.directory}/local-themes</outputDirectory>
                    <resources>
                        <resource>
                            <directory>${basedir}/src/main/webapp/themes</directory>
                            <excludes>
                                <exclude>**/*.bmp</exclude>
                                <exclude>**/*.jpg</exclude>
                                <exclude>**/*.jpeg</exclude>
                                <exclude>**/*.gif</exclude>
                                <exclude>**/*.png</exclude>
                                <exclude>**/*.ico</exclude>
                                <exclude>**/*.svg</exclude>
                                <include>**/*.ttf</include>
                                <include>**/*.woff</include>
                                <include>**/*.eot</include>
                            </excludes>
                            <filtering>true</filtering>
                        </resource>
                        <resource>
                            <directory>${basedir}/src/main/webapp/themes</directory>
                            <includes>
                                <include>**/*.bmp</include>
                                <include>**/*.jpg</include>
                                <include>**/*.jpeg</include>
                                <include>**/*.gif</include>
                                <include>**/*.png</include>
                                <include>**/*.ico</include>
                                <include>**/*.svg</include>
                                <include>**/*.ttf</include>
                                <include>**/*.woff</include>
                                <include>**/*.eot</include>
                            </includes>
                            <filtering>false</filtering>
                        </resource>
                    </resources>
                </configuration>
            </execution>
        </executions>
    </plugin>
    <plugin>
        <groupId>org.apache.maven.plugins</groupId>
        <artifactId>maven-antrun-plugin</artifactId>
        <version>1.1</version>
        <executions>
            <execution>
                <phase>validate</phase>
                <goals>
                    <goal>run</goal>
                </goals>
                <configuration>
                    <tasks>
                        <echoproperties>
                        </echoproperties>
                        <echo>${env.PATH}</echo>
                        <echo>${env.GEM_PATH}</echo>
                        <echo>${env.GEM_HOME}</echo>
                    </tasks>
                </configuration>
            </execution>
        </executions>
        <dependencies>
            <dependency>
                <groupId>org.apache.ant</groupId>
                <artifactId>ant</artifactId>
                <version>1.9.2</version>
            </dependency>
        </dependencies>
    <pin>
```

After that you’ll need to customise the existing maven-war plugin: */project/build/plugins/plugin[artifactId/text()='maven-war-plugin']*

Add this to the configuration section:

```xml
    <warSourceExcludes>themes/**</warSourceExcludes> 
```

Add this to the *configuration/webResources* section:
```xml
    <resource>
        <directory>${project.build.directory}/local-themes</directory>
        <targetPath>themes</targetPath>
        <excludes>
            <exclude>**/node_modules/**</exclude>
        </excludes>
    </resource>
```

And for each overlay (both *dspace-xmlui-lang* and *dspace-xmlui*) add
```xml
    <type>war</type>
```

and

```xml
    <excludes> 
         <exclude>WEB-INF/classes/**</exclude>
    </excludes>
```

Note that for *dspace-xmlui-lang* the *excludes* section already exists, so add the *exclude* line to the existing section.

Finally, add the following to */project/profiles* (this is no longer relative to the maven-war-plugin plugin):

```xml
    <profile>
        <id>npm</id>
        <activation>
            <activeByDefault>true</activeByDefault>
        </activation>
        <build>
            <plugins>
                <plugin>
                    <groupId>org.codehaus.mojo</groupId>
                    <artifactId>exec-maven-plugin</artifactId>
                    <version>1.2.1</version>
                    <executions>
                        <execution>
                            <id>bower</id>
                            <goals>
                                <goal>exec</goal>
                            </goals>
                            <phase>prepare-package</phase>
                            <configuration>
                                <executable>bower</executable>
                                <arguments>
                                    <argument>install</argument>
                                </arguments>
                                <workingDirectory>${project.build.directory}/local-themes/Mirage2</workingDirectory>
                                <environmentVariables>
                                    <PATH>${env.PATH}</PATH>
                                </environmentVariables>
                            </configuration>
                        </execution>
                        <execution>
                            <id>npm</id>
                            <goals>
                                <goal>exec</goal>
                            </goals>
                            <phase>prepare-package</phase>
                            <configuration>
                                <executable>npm</executable>
                                <arguments>
                                    <argument>install</argument>
                                </arguments>
                                <workingDirectory>${project.build.directory}/local-themes/Mirage2</workingDirectory>
                                <environmentVariables>
                                    <PATH>${env.PATH}</PATH>
                                </environmentVariables>
                            </configuration>
                        </execution>
                        <execution>
                            <id>grunt</id>
                            <goals>
                                <goal>exec</goal>
                            </goals>
                            <phase>prepare-package</phase>
                            <configuration>
                                <executable>grunt</executable>
                                <arguments>
                                    <argument>${grunt.color.scheme}</argument>
                                    <argument>${grunt.environment}</argument>                                
                                </arguments>
                                <workingDirectory>${project.build.directory}/local-themes/Mirage2</workingDirectory>
                                <environmentVariables>
                                    <PATH>${env.PATH}</PATH>
                                    <GEM_PATH>${env.GEM_PATH}</GEM_PATH>
                                    <GEM_HOME>${env.GEM_HOME}</GEM_HOME>
                                    <LC_CTYPE>UTF-8</LC_CTYPE>
                                </environmentVariables>
                            </configuration>
                        </execution>
                    </executions>
                </plugin>
            </plugins>
        </build>
    </profile>
    <profile>
        <id>grunt_dev</id>
        <properties>
            <grunt.environment>dev</grunt.environment>
        </properties>
    </profile>
    <profile>
        <id>grunt_bootstrap_style</id>
        <properties>
            <grunt.color.scheme>bootstrap_style</grunt.color.scheme>
        </properties>
    </profile>
```

For Windows: change version to 1.3.1:

```xml
<version>1.2.1</version>
```

#### Warning: ####
If you previously installed the alpha version of the theme, you'll notice that the *grunt_prod* profile doesn't exist anymore. Instead the grunt build environment is defined by the *grunt.environment* property in */project/properties* which is set to *prod*. By activating the *grunt_dev* profile the value can be overwritten to *dev*. 

Now, if you run *mvn package* in the dspace project root, bower will download all Mirage 2 dependencies, and grunt will trigger a number of plugins to preprocess, concatenate and minify all the theme's resources. 

## Production and development builds ##

You can use maven profiles to have grunt build the theme in production or development mode. 

In production mode, grunt will concatenate and minify all javascript files referenced in the *Mirage2/scripts.xml* file (for more details see the javascript section of this document) in to a single *theme.js* file, to improve the performance of the theme. In development mode all javascript files will be separate and untouched, to make debugging easier.

Similarly for CSS, compass will compile the CSS either using *Mirage2/config-dev.rb* or *Mirage2/config-prod.rb*. Both will yield a single css file: main.css, but the dev version won't be minified and will contain the scss file name and line number as a comment above each CSS rule.

By default grunt will build the javascript and CSS in production mode. Use the *grunt_dev* maven profile, or run grunt with the *dev* task to build the theme in development mode.

#### Note: ####
The dspace.cfg property *xmlui.minifiedjs* used in the alpha version of the theme to switch between development and production modes has been deprecated. Because we've moved the javascript declarations to a separate file: *scripts.xml* we no longer need to know which mode you're using in the theme XSLs at runtime but instead can let grunt handle it at build time.

## DRI Pre-Processing ##

One of the goals for this theme was that changes outside the theme’s directory should be kept to an absolute minimum, so other themes won’t be affected by changes made for Mirage 2. But to make a responsive theme we needed to do some major restructuring of DSpace’s UI elements. The traditional way of doing so in XMLUI is adding slightly tweaked copies of default templates to the theme XSL. 

For example, if you want to add an extra class to div, you’d copy the default div template, change the copy's matcher to match the div you want to target, and then add your class to that template. That adds about 10 lines of XSL, only to add a simple CSS class. It makes the theme XSL files much harder to read and to modify; because if you wanted to change the default div template afterwards, you'd also have to change every template that overrides it.

So we added an additional XSL transformation step in the theme’s sitemap: preprocess.xsl. This transformation is added right after the DRI generator, and it transforms DRI to DRI. By default it will simply copy the input to the output, and if you want to do something that is perfectly possible in DRI, like adding an extra CSS class to a div, you can now add your exception to the preprocess XSL, and simply let the default div template of the theme XSL render it afterwards. 

#### Tip: ####
When working with a DRI that has been changed by the preprocess XSLs, it is often useful to see the differences between the original DRI and the version after preprocessing. To see the original DRI you can add *DRI/* after the contextpath in a page’s URL, to see the preprocessed DRI add *?XML* after the page’s URL

## Style ##

Mirage 2 contains two color schemes to choose from. The classic Mirage color scheme or the standard Bootstrap color scheme. By default grunt will build CSS to get the classic Mirage color scheme. However, by activating the *grunt_bootstrap_style* maven profile, this can be changed to get the standard Bootstrap color scheme. 

The stylesheets are written in [SASS](http://sass-lang.com/). You can find them in the folder *Mirage2/styles*. Depending on whether you use the Mirage or Bootstrap color scheme, either *_main_classic_mirage_style.scss* or *_main_bootstrap_style.scss* is the root-file that imports all others. Some imports might be nested, but eventually the sequence will be to import the Compass library first. Next, all Bootstrap scss files and finally the DSpace specific files. You'll notice that most of the scss files in the styles folder are only used by the classic Mirage color scheme.  When you use the Bootstrap color scheme those files are not imported in the root scss file and they will not end up in the compiled CSS file. 

The goal of the standard Bootstrap color scheme was to add as little extra style on top of Bootstrap as possible and instead force ourselves to solve most issues in XSL; by creating the right HTML structure and adding the right bootstrap CSS classes. But try as we might, a few additional style rules were still needed. Those can be found in *_dspace-bootstrap-tweaks.scss*, most of it is limited to styles to improve the sidebar behavior on mobile devices.

The classic Mirage color scheme contains more modifications and even overwrites a Bootstrap file with a local copy. In the folder *Mirage2/styles/bootstrap_overrides/_variables.scss*. It contains all the SASS variables used by bootstrap and a few extra’s added for DSpace. Unfortunately you can’t change the value of a SASS variable once it’s set, so that’s why we have to replace the original bootstrap *_variables.scss* file with our own slightly modified copy.

The variables file is important because it allows you to change the look of the theme dramatically, simply by changing a few color codes or the font-family. Try to change the value of *$brand-primary* to see what we mean.

If you want to base your theme on an existing Bootstrap theme (like the ones at [bootswatch.com](http://bootswatch.com)) you can do so by using the standard Bootstrap style and replacing the import of Bootstrap in *_main_bootstrap_style.scss*:

```java
    @import "../vendor/bootstrap-sass-official/vendor/assets/stylesheets/bootstrap";
```

with an import of just the *_variables.sccs* file (we need those variables in *_dspace-bootstrap-tweaks.scss*):

```java
    @import "../vendor/bootstrap-sass-official/vendor/assets/stylesheets/bootstrap/_variables";
```

Then import the the css file(s) of your Bootstrap theme of choice below it. Depending on the theme you may also need to update the *twbs-font-path* function right above that import statement.

#### Tip: ####
During development it is a hassle to always have to run *mvn package* to re-compile the style and see the result of a CSS change. Luckily compass comes with a “watch” feature that automatically recompiles when the scss files change. If your editor can update the running webapp when you save an scss file, and you run *compass watch* in that webapp's *xmlui/themes/Mirage2* folder, changes to the style will be nearly instantaneous.


## Scripts ##

Any javascript file that you reference in the file *Mirage2/scripts.xml* will be included in the production *theme.js* file and will be loaded separately and unmodified in development mode. 

The tradeoff of having all the site’s javascript in a minified single file is that all scripts are loaded on every page. That means you’ll have to be extra careful that any script you write only targets the DOM elements you intend to target, and nothing else (meaning, target DOM elements using their IDs and/or very specific class names). In the future we may look into using a framework like [require.js](http://requirejs.org/) to retain these advantages while still only loading a script when it’s needed.

To keep global javascript variables to a minimum, we’ve created a namespace object called *DSpace* to contain all other global variables. For example *DSpace.context_path* and *DSpace.theme_path* contain the context and theme paths, *DSpace.templates* contains compiled handlebars templates, and *DSpace.i18n* contains i18n strings used by those templates (take a look at the object in your browser’s dev-tools on a discovery page to see that in action). We advise you to put your own global javascript objects in the *DSpace* namespace as well.

If you create your own handlebars templates, put them in *Mirage2/templates*. They will be precompiled and added to *theme.js* by grunt. To access a template in your javascript code, use the function *DSpace.getTemplate(file-name)* (where *file-name* is the name of your handlebars file, without the extension). That function will return a precompiled version of the template if it exists, and download and compile it if it doesn't. That ensures your templates work both when you’re in development mode or using the production *theme.js* file.

The theme also comes with built in support for [CoffeeScript](http://coffeescript.org/). If you put *.coffee* files in the *Mirage2/scripts* folder, they will be converted to javascript. Make sure to add the correct reference to *Mirage2/scripts.xml* i.e. with an *.js* extension instead of *.coffee*.

## Managing dependencies ##

Mirage 2’s dependencies are specified in the file *Mirage2/bower.json*. Dependencies in this file should be specified according to the rules in the [Bower documentation](http://bower.io/|). Note that Bower only downloads the dependencies, nothing more. So if you add other dependencies, you'll still have to reference them. That means if it’s a CSS file, import it in *Mirage2/styles/_main_classic_mirage_style.scss* or *Mirage2/styles/_main_bootstrap_style.scss* (depending on the color scheme you're using), if it's a javascript file, add it to *Mirage2/scripts.xml*.

We've used the the "*latest*" keyword to specify dependency versions in *bower.json* wherever possible because that ensures you're starting with up to date versions when creating a new theme. However once your theme is going in to production, we recommend replacing "*latest*" with the actual version numbers being used at that moment. That way your production build won't break when a version of a dependency is released that isn't backwards compatible.