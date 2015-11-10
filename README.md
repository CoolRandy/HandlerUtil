# HandlerUtil
总结关于android所有handler相关的知识点

这个仓库中的代码全部是用于对Handler进行测试的一些demo，主要包括对于handler分别在UI线程以及子线程中的使用
此外还有对于HandlerThread的使用，具体后面会所有的类文件做一个详细说明，请稍后^_^
-------------------------

对各类的说明如下：
HandlerThreadActivity：该类主要用于对HandlerThread的使用做一番研究，具体使用代码中描述的很清楚，这里我主要说一下HandlerThread的作用，实际上HandlerThread继承于Thread，就是Thread的子类，只是Android系统对它做了一些封装，该方法中调用了Loop.prepare()和Loop.loop()方法，在传统的方法里worker thread如果在内部创建handler，则需要自己手动调用Loop的两个方法，而HandlerThread则已经封装好了

这样直接通过HandlerThread实例获取looper对象，然后通过looper对象来实例化Handler，接下来就可以在handler重写的handleMessage里处理一些耗时任务了，因为handler是运行在HandlerThread子线程中，具体使用详见代码哈
