# ESLint Plugin #

<a href="http://eslint.org/">ESLint</a> is The pluggable linting utility for JavaScript. see more <a href="http://eslint.org/">here</a>.<br/>
ESLint plugin for WebStorm, PHPStorm and other Idea family IDE with Javascript plugin, provides integration with ESLint and shows errors and warnings inside the editor.

## Getting started ##
### Prerequisites ###
If you do not have nodejs installed on your machine, download and install <a href="http://nodejs.org/">NodeJS</a>.<br/>

Install eslint npm package <a href="https://www.npmjs.org/package/eslint">eslint npm</a>:<br/>
`$ cd <project path>`<br/>
`$ npm install eslint`<br/>
Or, install eslint globally:<br/>
`$ npm install -g eslint`<br/>

### Settings ###
To get started, you need to set the ESLint plugin settings:<br/>

* Go to preferences, ESLint plugin page and check the Enable plugin.
* Set the path to the nodejs interpreter bin file.
* Select whether to let eslint search for .eslintrc file
* Set the path to the eslint bin file. should point to <project path>node_modules/eslint/bin/eslint.js if you installed locally or /usr/local/bin/eslint if you installed globally.
* Set the .eslintrc file, or eslint will use the default settings.
* You can also set a path to a custom rules directory.

Configuration:<br/>
![ESLint config](https://raw.githubusercontent.com/idok/eslint-plugin/master/doc/config.png)


Inspection:<br/>
![ESLint inline](https://raw.githubusercontent.com/idok/eslint-plugin/master/doc/inspect-inline.png)


Analyze Code:<br/>
![ESLint inline](https://raw.githubusercontent.com/idok/eslint-plugin/master/doc/inspect.png)
