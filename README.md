# transit-clj-0.8.287-memory-leak

This is a demonstration of the memory leak present in transit-clj 0.8.287.  The
leak is a result of the `WriteHandler` cache added to transit-java
[7c3d9](https://github.com/cognitect/transit-java/commit/7c3d9c8ca7495bfa28488ebf94f5cc72ef0627bd).
Every time you create a writer, transit-clj produces a new hash map of write
handlers by calling `cognitect.transit/default-write-handlers`, and the
`WriterFactory` class will cache them.

Since the maps produced by `default-write-handlers` are not equal to each other,
the write handler cache accrues a new entry every time you create a writer.
Eventually the JVM will give an OOME (either overhead limit or out of memory).

Presumably the same problem exists for the `ReadHandler` cache, but I have not
verified.

## Usage

This project has a main function that will attempt to transit encode 20,000 hash
maps in chunks of 1,000.  Before starting to encode, it prints the JVM max heap
and the version of transit-clj that it is using.  After each chunk of 1,000 hash
maps has been encoded, it will print the number of milliseconds it took to
complete.

The project.clj sets the max heap to 50MB, which is enough to cause an overhead
limit OOME when running the main function with transit-clj 0.8.271.

    >paul@mbp:~/src/transit-clj-0.8.271-memory-leak$ lein run
    >Max Heap: 50331648
    >transit-clj Version: 0.8.271
    >Encoded chunk of 1000 (129 ms)
    >Encoded chunk of 1000 (39 ms)
    >Encoded chunk of 1000 (34 ms)
    >Encoded chunk of 1000 (29 ms)
    >Encoded chunk of 1000 (26 ms)
    >Encoded chunk of 1000 (26 ms)
    >Encoded chunk of 1000 (23 ms)
    >Encoded chunk of 1000 (24 ms)
    >Encoded chunk of 1000 (22 ms)
    >Encoded chunk of 1000 (166 ms)
    >Encoded chunk of 1000 (18 ms)
    >Encoded chunk of 1000 (20 ms)
    >Encoded chunk of 1000 (230 ms)
    >Encoded chunk of 1000 (136 ms)
    >Encoded chunk of 1000 (362 ms)
    >Encoded chunk of 1000 (461 ms)
    >Encoded chunk of 1000 (855 ms)
    >java.lang.reflect.InvocationTargetException
    >    at sun.reflect.GeneratedConstructorAccessor1.newInstance(Unknown Source)
    >    at sun.reflect.DelegatingConstructorAccessorImpl.newInstance(DelegatingConstructorAccessorImpl.java:45)
    >    at java.lang.reflect.Constructor.newInstance(Constructor.java:408)
    >    at org.msgpack.template.builder.TemplateBuilderChain.createForceTemplateBuilder(TemplateBuilderChain.java:82)
    >    at org.msgpack.template.builder.TemplateBuilderChain.reset(TemplateBuilderChain.java:68)
    >    at org.msgpack.template.builder.TemplateBuilderChain.<init>(TemplateBuilderChain.java:49)
    >    at org.msgpack.template.builder.TemplateBuilderChain.<init>(TemplateBuilderChain.java:44)
    >    at org.msgpack.template.TemplateRegistry.createTemplateBuilderChain(TemplateRegistry.java:102)
    >    at org.msgpack.template.TemplateRegistry.<init>(TemplateRegistry.java:95)
    >    at org.msgpack.MessagePack.<init>(MessagePack.java:60)
    >    at com.cognitect.transit.impl.WriterFactory.getMsgpackInstance(WriterFactory.java:137)
    >    at com.cognitect.transit.TransitFactory.writer(TransitFactory.java:67)
    >    at cognitect.transit$writer.invoke(transit.clj:134)
    >    at cognitect.transit$writer.invoke(transit.clj:130)
    >    at transit_clj_memory_leak.core$transit_encode.invoke(core.clj:9)
    >    at transit_clj_memory_leak.core$_main$fn__161.invoke(core.clj:25)
    >    at transit_clj_memory_leak.core$_main.invoke(core.clj:24)
    >    at transit_clj_memory_leak.core$_main.invoke(core.clj:14)
    >    at clojure.lang.Var.invoke(Var.java:375)
    >    at user$eval5.invoke(form-init8765779360177825803.clj:1)
    >    at clojure.lang.Compiler.eval(Compiler.java:6703)
    >    at clojure.lang.Compiler.eval(Compiler.java:6693)
    >    at clojure.lang.Compiler.load(Compiler.java:7130)
    >    at clojure.lang.Compiler.loadFile(Compiler.java:7086)
    >    at clojure.main$load_script.invoke(main.clj:274)
    >    at clojure.main$init_opt.invoke(main.clj:279)
    >    at clojure.main$initialize.invoke(main.clj:307)
    >    at clojure.main$null_opt.invoke(main.clj:342)
    >    at clojure.main$main.doInvoke(main.clj:420)
    >    at clojure.lang.RestFn.invoke(RestFn.java:421)
    >    at clojure.lang.Var.invoke(Var.java:383)
    >    at clojure.lang.AFn.applyToHelper(AFn.java:156)
    >    at clojure.lang.Var.applyTo(Var.java:700)
    >    at clojure.main.main(main.java:37)
    >Caused by: java.lang.OutOfMemoryError: GC overhead limit exceeded
    >    at java.util.Hashtable.<init>(Hashtable.java:190)
    >    at java.util.Hashtable.<init>(Hashtable.java:203)
    >    at javassist.ClassPool.<init>(ClassPool.java:188)
    >    at javassist.ClassPool.<init>(ClassPool.java:162)
    >    at org.msgpack.template.builder.JavassistTemplateBuilder.<init>(JavassistTemplateBuilder.java:61)
    >    at sun.reflect.GeneratedConstructorAccessor1.newInstance(Unknown Source)
    >    at sun.reflect.DelegatingConstructorAccessorImpl.newInstance(DelegatingConstructorAccessorImpl.java:45)
    >    at java.lang.reflect.Constructor.newInstance(Constructor.java:408)
    >    at org.msgpack.template.builder.TemplateBuilderChain.createForceTemplateBuilder(TemplateBuilderChain.java:82)
    >    at org.msgpack.template.builder.TemplateBuilderChain.reset(TemplateBuilderChain.java:68)
    >    at org.msgpack.template.builder.TemplateBuilderChain.<init>(TemplateBuilderChain.java:49)
    >Exception in thread "main" java.lang.RuntimeException: java.lang.OutOfMemoryError: GC overhead limit exceeded, compiling:(/private/var/folders/0x/7d0trrc17md27slqg9rx5xtc0000gn/T/form-init8765779360177825803.clj:1:124)
    >    at clojure.lang.Compiler.load(Compiler.java:7142)
    >    at clojure.lang.Compiler.loadFile(Compiler.java:7086)
    >    at clojure.main$load_script.invoke(main.clj:274)
    >    at clojure.main$init_opt.invoke(main.clj:279)
    >    at clojure.main$initialize.invoke(main.clj:307)
    >    at clojure.main$null_opt.invoke(main.clj:342)
    >    at clojure.main$main.doInvoke(main.clj:420)
    >    at clojure.lang.RestFn.invoke(RestFn.java:421)
    >    at clojure.lang.Var.invoke(Var.java:383)
    >    at clojure.lang.AFn.applyToHelper(AFn.java:156)
    >    at clojure.lang.Var.applyTo(Var.java:700)
    >    at clojure.main.main(main.java:37)
    >Caused by: java.lang.RuntimeException: java.lang.OutOfMemoryError: GC overhead limit exceeded
    >    at com.cognitect.transit.TransitFactory.writer(TransitFactory.java:76)
    >    at cognitect.transit$writer.invoke(transit.clj:134)
    >    at cognitect.transit$writer.invoke(transit.clj:130)
    >    at transit_clj_memory_leak.core$transit_encode.invoke(core.clj:9)
    >    at transit_clj_memory_leak.core$_main$fn__161.invoke(core.clj:25)
    >    at transit_clj_memory_leak.core$_main.invoke(core.clj:24)
    >    at transit_clj_memory_leak.core$_main.invoke(core.clj:14)
    >    at clojure.lang.Var.invoke(Var.java:375)
    >    at user$eval5.invoke(form-init8765779360177825803.clj:1)
    >    at clojure.lang.Compiler.eval(Compiler.java:6703)
    >    at clojure.lang.Compiler.eval(Compiler.java:6693)
    >    at clojure.lang.Compiler.load(Compiler.java:7130)
    >    ... 11 more
    >Caused by: java.lang.OutOfMemoryError: GC overhead limit exceeded
    >    at java.util.Arrays.copyOfRange(Arrays.java:3658)
    >    at java.lang.String.<init>(String.java:201)
    >    at java.lang.StringBuilder.toString(StringBuilder.java:407)
    >    at java.lang.StackTraceElement.toString(StackTraceElement.java:173)
    >    at java.lang.String.valueOf(String.java:2979)
    >    at java.lang.StringBuilder.append(StringBuilder.java:131)
    >    at java.lang.Throwable.printEnclosedStackTrace(Throwable.java:697)
    >    at java.lang.Throwable.printStackTrace(Throwable.java:667)
    >    at java.lang.Throwable.printStackTrace(Throwable.java:643)
    >    at java.lang.Throwable.printStackTrace(Throwable.java:634)
    >    at org.msgpack.template.builder.TemplateBuilderChain.createForceTemplateBuilder(TemplateBuilderChain.java:84)
    >    at org.msgpack.template.builder.TemplateBuilderChain.reset(TemplateBuilderChain.java:68)
    >    at org.msgpack.template.builder.TemplateBuilderChain.<init>(TemplateBuilderChain.java:49)
    >    at org.msgpack.template.builder.TemplateBuilderChain.<init>(TemplateBuilderChain.java:44)
    >    at org.msgpack.template.TemplateRegistry.createTemplateBuilderChain(TemplateRegistry.java:102)
    >    at org.msgpack.template.TemplateRegistry.<init>(TemplateRegistry.java:95)
    >    at org.msgpack.MessagePack.<init>(MessagePack.java:60)
    >    at com.cognitect.transit.impl.WriterFactory.getMsgpackInstance(WriterFactory.java:137)
    >    at com.cognitect.transit.TransitFactory.writer(TransitFactory.java:67)
    >    at cognitect.transit$writer.invoke(transit.clj:134)
    >    at cognitect.transit$writer.invoke(transit.clj:130)
    >    at transit_clj_memory_leak.core$transit_encode.invoke(core.clj:9)
    >    at transit_clj_memory_leak.core$_main$fn__161.invoke(core.clj:25)
    >    at transit_clj_memory_leak.core$_main.invoke(core.clj:24)
    >    at transit_clj_memory_leak.core$_main.invoke(core.clj:14)
    >    at clojure.lang.Var.invoke(Var.java:375)
    >    at user$eval5.invoke(form-init8765779360177825803.clj:1)
    >    at clojure.lang.Compiler.eval(Compiler.java:6703)
    >    at clojure.lang.Compiler.eval(Compiler.java:6693)
    >    at clojure.lang.Compiler.load(Compiler.java:7130)
    >    at clojure.lang.Compiler.loadFile(Compiler.java:7086)
    >    at clojure.main$load_script.invoke(main.clj:274)

If you give an argument to main it will memoize
`cognitect.transit/default-write-handlers` which will avoid the OOME.

    >paul@mbp:~/src/transit-clj-0.8.271-memory-leak$ lein run memoize
    >Max Heap: 50331648
    >transit-clj Version: 0.8.271
    >Encoded chunk of 1000 (108 ms)
    >Encoded chunk of 1000 (26 ms)
    >Encoded chunk of 1000 (22 ms)
    >Encoded chunk of 1000 (21 ms)
    >Encoded chunk of 1000 (20 ms)
    >Encoded chunk of 1000 (20 ms)
    >Encoded chunk of 1000 (17 ms)
    >Encoded chunk of 1000 (18 ms)
    >Encoded chunk of 1000 (18 ms)
    >Encoded chunk of 1000 (15 ms)
    >Encoded chunk of 1000 (15 ms)
    >Encoded chunk of 1000 (15 ms)
    >Encoded chunk of 1000 (11 ms)
    >Encoded chunk of 1000 (12 ms)
    >Encoded chunk of 1000 (15 ms)
    >Encoded chunk of 1000 (13 ms)
    >Encoded chunk of 1000 (11 ms)
    >Encoded chunk of 1000 (11 ms)
    >Encoded chunk of 1000 (15 ms)
    >Encoded chunk of 1000 (15 ms)

If you run main with the `transit-clj-0.8.269` profile, then it will use
version 0.8.269 of transit-clj, which does not have the memory leak.

    >paul@mbp:~/src/transit-clj-0.8.271-memory-leak$ lein with-profile transit-clj-0.8.269 run
    >Max Heap: 50331648
    >transit-clj Version: 0.8.269
    >Encoded chunk of 1000 (125 ms)
    >Encoded chunk of 1000 (40 ms)
    >Encoded chunk of 1000 (28 ms)
    >Encoded chunk of 1000 (25 ms)
    >Encoded chunk of 1000 (25 ms)
    >Encoded chunk of 1000 (23 ms)
    >Encoded chunk of 1000 (22 ms)
    >Encoded chunk of 1000 (18 ms)
    >Encoded chunk of 1000 (21 ms)
    >Encoded chunk of 1000 (17 ms)
    >Encoded chunk of 1000 (18 ms)
    >Encoded chunk of 1000 (22 ms)
    >Encoded chunk of 1000 (20 ms)
    >Encoded chunk of 1000 (19 ms)
    >Encoded chunk of 1000 (19 ms)
    >Encoded chunk of 1000 (20 ms)
    >Encoded chunk of 1000 (17 ms)
    >Encoded chunk of 1000 (17 ms)
    >Encoded chunk of 1000 (15 ms)
    >Encoded chunk of 1000 (19 ms)


## License

Copyright © 2015 Paul Stadig.

Distributed under the Eclipse Public License either version 1.0 or (at your
option) any later version.
