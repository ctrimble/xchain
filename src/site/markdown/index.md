# XChain Framework

The XChain project is a fusion of the Apache Chain and Apache JXPath projects, to create an XML based scripting language.  This is a dump of the head of our SVN repository, with some modificatiosn made to allow the project to build publically.  Although the code in this package was used in production, several of the dependencies had to be reverted to SNAPSHOTs to get the code to build publicly, so be aware of that when using this project.

## This documentation

The hand written documentation in this site is sparse, but here are some deep links to examples that exist:

- [Creating Commands](./xchain-core/guides/command-definition.html) - An example for defining a command.
- [SAX Events](./xchain-core/guides/commands-that-output-sax.html) - An example of using SAX event streams with XChains.

## The code base

These are some interesting spots in the code base that you may want to look at:

- [Main Interfaces](https://github.com/ctrimble/xchain/tree/master/core/src/main/java/org/xchain) - The package that contains the definitions for Command, Chain, etc.
- [Factories](https://github.com/ctrimble/xchain/tree/master/core/src/main/java/org/xchain/framework/factory) - The factories for catalogs and SAX templates.
- [Lifecycle](https://github.com/ctrimble/xchain/blob/master/core/src/main/java/org/xchain/framework/lifecycle/Lifecycle.java) - The class that starts the framework.
- [Core Commands](https://github.com/ctrimble/xchain/tree/master/core/src/main/java/org/xchain/namespaces/core) - The package that contains commands for program flow, variables, loops, etc.
- [SAX Commands](https://github.com/ctrimble/xchain/tree/master/core/src/main/java/org/xchain/namespaces/sax) - The package that contains commands for SAX pipelines.
- [Servlet Commands](https://github.com/ctrimble/xchain/tree/master/container/src/main/java/org/xchain/namespaces/servlet) - The package that contains commands for Servlets.

