$(document).ready(
    function() {
        $("button.main-navi").click(function() {
            $("#showcarry").css('display','none');
            $(".mobile-navi").fadeIn(50);
            $("#hidecarry").show();
            // cdg.style.height = (window.innerHeight - 200) + "px";
            // cdg.style.width = (window.innerWidth - 400) + "px";
            $("#grid").css('width', '72%');
        });
        $("button.Close").click(function() {
            $("#hidecarry").css('display','none');
            $(".mobile-navi").fadeOut(50);
            $("#showcarry").show();
            // cdg.style.height = (window.innerHeight - 200) + "px";
            $("#grid").css('width', '100%');
        });
    });
