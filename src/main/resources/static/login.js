$(document).ready(function(){

    var headers = new Headers();

    $("#login-form").submit(function(e) {
        e.preventDefault();

        var xhr = new XMLHttpRequest();
        var url = '/login';
    
        xhr.onreadystatechange = function(){
            if(xhr.readyState === 4){
                if(xhr.responseText != 'Authentication failed'){
                    var token = xhr.responseText
                    console.log(token);
                    document.cookie = token;
                } else if(xhr.status === 403){
                    $("#modal").dialog("open");
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
