// Copyright (c) 2015 University of South Florida

** Introduction: **
This is a research-quality implementation of the PoCo language and
system developed by Yan Albright, Danielle Ferguson, Cory Juhlin, 
Jay Ligatti, and Daniel Lomsak.  As a research implementation, it is 
highly unoptimized and contains bugs. However, the system has sufficient 
performance and reliability to test many complex designs of policy 
composition.  

We would love to hear about your improvements to and use of PoCo.
You can contact us at the following email addresses:
dferguson@cse.usf.edu, ligatti@cse.usf.edu, yalbright@mail.usf.edu,
or cjuhlin@mail.usf.edu


** Installation instructions: **
NEED TO ADD


** Further Examples: **
NEED TO ADD


** Usage Notes: **
Actions MUST be fully qualified in this version of PoCo.  For
example, writing the action <Action (`MyClass.foo(#String[%])') => Neutral> 
will not work; instead write <Action (`mypackage.MyClass.foo(#java.lang.String[%])') => Neutral>.



This release does not allow interception of the following sorts of 
Java methods: 
NEED TO ADD

** Known Bugs/Issues: **
This implementation does not allow policies to be included from more than one file

** Included Software: **
This distribution of Polymer contains versions of: 
--dk.brics.automaton, http://www.brics.dk/automaton/
--ANTLR, http://www.antlr.org/
--AspectJ, https://eclipse.org/aspectj/
--ASM, http://asm.ow2.org/
--JOpt simple, http://pholser.github.io/jopt-simple/
Licensing/copyright information can be found in respective subdirectories.






