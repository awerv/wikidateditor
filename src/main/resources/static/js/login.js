$(document).ready(function(){

    var headers = new Headers();

    $("#login-form").submit(function(e) {
        e.preventDefault();

        var xhr = new XMLHttpRequest();
        var url = '/login';
    
        xhr.onreadystatechange = function(){
            if(xhr.readyState === 4){
                var msg = xhr.responseText
                if(xhr.responseText === 'ALREADYIN'){
                    console.log(msg);
                } else if(xhr.responseText === 'AUTHFAIL'){
                    $("#modal").dialog("open");
                } else if(xhr.responseText === 'AUTHOK'){
                    console.log(msg);
                    window.location.replace('/');
                }
            }
        }
    
        xhr.open("POST", url);
        xhr.setRequestHeader("Content-Type", "application/json");
        xhr.send(JSON.stringify({ username: $('#login').val(), password: $('#password').val() }));
    
    });
    
    $("#modal").dialog({
        autoOpen : false, modal : true, show : "blind", hide : "blind"
    });
});
