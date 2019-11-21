$(document).ready(
    function() {
        $("button.main-nav").click(function() {
            $("#showcarry").css('display','none');
            $(".mobile-nav").fadeIn(50);
            $("#hidecarry").show();
            // cdg.style.height = (window.innerHeight - 200) + "px";
            // cdg.style.width = (window.innerWidth - 400) + "px";
            _resize()
            cdg.style.width = (window.innerWidth - 400) + "px";

        });
        $("button.Close").click(function() {
            $("#hidecarry").css('display','none');
            $(".mobile-nav").fadeOut(50);
            $("#showcarry").show();
            // cdg.style.height = (window.innerHeight - 200) + "px";
            cdg.style.width = (window.innerWidth - 100) + "px";
            _resize()
        });
    });