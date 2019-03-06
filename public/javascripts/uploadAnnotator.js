    var r = new Resumable({
        target:'/hadatac/annotator/uploadfile',
        chunkSize:10*1024,
        simultaneousUploads:4,
        testChunks: true,
        throttleProgressCallbacks:1,
        method: "octet"
      });
