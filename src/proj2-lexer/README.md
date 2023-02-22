For the preprocessor lines, I am using the JFlex yypushStream and yypopStream methods, which are only available in an additional `skeleton.nested` file that can be found from the JFlex github repo. I have included this file, since you need to make sure it matches the version of JFlex being ran (the version I grabbed was with the 1.6.1 release).

Per the documentation, you need to run JFlex with the `--skel` flag as such:
`java -jar jflex-1.6.1.jar --skel skeleton.nested Lexer.flex`
