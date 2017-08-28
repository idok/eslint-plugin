# ESLint Plugin #

[ESLint](http://eslint.org/) is The pluggable linting utility for JavaScript. see more [here](http://eslint.org/).<br/>
ESLint plugin for WebStorm, PHPStorm and other Idea family IDE with Javascript plugin, provides integration with ESLint and shows errors and warnings inside the editor.
* Support displaying eslint warnings as intellij inspections
* Quick fixes for several rules
* Support for custom eslint rules

## Bundled plugin ##
As of Intellij 14, a plugin based on this one was bundled into the IDE release by Jetbrains.
What's the diffrence?
This plugin supports Intellij 13 and other versions, --fix option, quick fixes and other minor differences.
Please make sure you are referring to this one before opening an issue.


## Getting started ##
### Prerequisites ###
* [NodeJS](http://nodejs.org/)
* IntelliJ 13.1.4 / Webstorm 8.0.4, or above.

Install eslint npm package [eslint npm](https://www.npmjs.org/package/eslint)</a>:<br/>
```bash
$ cd <project path>
$ npm install eslint
```
<b>(Recommend!)</b>
Or, install eslint globally, REMEMBER the eslint plugins followed:<br/>
```bash
$ sudo npm install -g eslint
$ sudo npm install -g eslint-config-standard
$ sudo npm install -g eslint-plugin-import
$ sudo npm install -g eslint-plugin-node
$ sudo npm install -g eslint-plugin-promise
$ sudo npm install -g eslint-plugin-standard
```

### Using in CLI ###
To get started with CLI, you need to get familiar with the following cmd:<br/>

* Lint folder/project
```bash
$ eslint ./controllers/.
```
Output:<br/>
__/path/to/dcos_ops/web/controllers/war/action/create.js__<br/>
__1:7  error  'config' is assigned a value but never used  no-unused-vars__<br/>
_Note: you could fix your codes upon the 'error'_

* Lint single target js file
```bash
$ eslint ./controllers/war/action/create.js
```
Output:<br/>
_Note: after you have fix the 'error', the output will be nothing_

### Settings ###
To get started with Idea, you need to set the ESLint plugin settings:<br/>

* Go to preferences, ESLint plugin page and check the Enable plugin.
* Set the path to the nodejs interpreter bin file.
* Select whether to let eslint search for ```.eslintrc``` file
* Set the path to the eslint bin file. should point to ```<project path>node_modules/eslint/bin/eslint.js``` if you installed locally or ```/usr/local/bin/eslint``` if you installed globally.
  * For Windows: install eslint globally and point to the eslint cmd file like, e.g.  ```C:\Users\<username>\AppData\Roaming\npm\eslint.cmd```
* Set the ```.eslintrc``` file, or eslint will use the default settings.
* You can also set a path to a custom rules directory.
* By default, eslint plugin annotate the editor with warning or error based on the eslint configuration, you can check the 'Treat all eslint issues as warnings' checkbox to display all issues from eslint as warnings.

Configuration:<br/>
![ESLint config](https://raw.githubusercontent.com/idok/eslint-plugin/master/doc/config.png)


Inspection:<br/>
![ESLint inline](https://raw.githubusercontent.com/idok/eslint-plugin/master/doc/inspect-inline.png)


Analyze Code:<br/>
![ESLint inline](https://raw.githubusercontent.com/idok/eslint-plugin/master/doc/inspect.png)

### A Note to contributors ###
ESLint plugin uses the code from [here](https://github.com/idok/scss-lint-plugin/tree/master/intellij-common) as a module, to run the project you need to clone that project as well.

