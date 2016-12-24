var current_channel = "";
/****************Cookie************************/
function getCookie(cname) {
    var name = cname + "=";
    var ca = document.cookie.split(';');
    for(var i = 0; i <ca.length; i++) {
        var c = ca[i];
        while (c.charAt(0) == ' ') {
            c = c.substring(1);
        }
        if (c.indexOf(name) == 0) {
            return c.substring(name.length, c.length);
        }
    }
    return "";
}

/****************WebSocket********************/
var eb = new EventBus("/eventbus/");

eb.onopen = function () {
  eb.registerHandler("chat.to.client", function (err, msg) {
    
    addMessage(msg.body.content, "left", msg.body.username, msg.body.date);
    
    $("#chat-list").scrollDown($("#chat-list").scrollDown() - 100);
  });
}


function send(event) {
  var user = getCookie("user");
  if ( current_channel != ""){
    if (event.keyCode == 13 || event.which == 13) {
      var message = {
        content : document.getElementById('input').value,
        channel : current_channel,
        username : user
      };
      if (message.content.length > 0) {
        eb.publish("chat.to.server", JSON.stringify(message));
        document.getElementById('input').value = "";
      }
    }
  } else {
    alert("First, select a channel");
  }
}
/********************************************************/

function addMessage(msg, dir, user, time) {
	html = "<li class="+dir+" clearfix\">"+
            	"<div class=\"chat-body clearfix\">"+
            		"<div class=\"header\">"+
            			"<strong class=\"primary-font\">"+user+"</strong>"+
            			"<small class=\"pull-right text-muted\"><i class=\"fa fa-clock-o\"></i> "+time+" </small>"+
            		"</div>"+
            		"<p>"+msg+
            		"</p>"
            	"</div>"+
            "</li>";
	document.getElementById('chat-list').innerHTML += html;
}

function addChannel(title){
	html = "<li class=\"active bounceInDown\">"+
            "<a onclick=loadChannel("+"'"+title+"'"+") class=\"clearfix\">"+
              "<div class=\"friend-name\">"+ 
                "<strong>"+title+"<span class=\"btn-danger closebtn badge newchannel-btn\" onclick=\"deleteCh("+"'"+title+"'"+")\" id=\"btn-delete\">x</span></strong>"+
              "</div>"+
            "</a>"+
          "</li>";
  document.getElementById('channel-list').innerHTML += html;
}

function loadChannel(title) {
	document.getElementById('chat-list').innerHTML = "";
	loadMessageList(title);
  current_channel = title;
}

function loadChannelList(){
	var div = document.getElementById('channel-list');
  div.innerHTML = "";
  $.ajax({
    url: "http://localhost:9997/channels",
    type: "GET",
    dataType: "json",
    success: function(res){
      for (var i in res){
      	if(document.getElementById(res[i].name) == undefined){
        		addChannel(res[i].name);
        }
      }
    }
  });
}

function loadMessageList(channel_title){
    var div = document.getElementById('chat-list');
    current_channel = channel_title;
    $.ajax({
        url: "http://localhost:9997/messages/"+channel_title,
        type: 'GET',
        dataType: 'json',
        success: function(res) {
          if(res.length == 0){
            div.innerHTML = "";
          } else {
            for (var i in res){
            	addMessage(res[i].content, "left", res[i].username, res[i].time)
            }
            div.scrollTop = div.scrollHeight;
          }
        }
    });
}

function deleteCh(title) {
	var json = {
		title : title
	};
	$.ajax({
		type: 'POST',
		url: '/deleteCh',
		data: json,
		dataType : 'json'
	});
  loadChannelList();
}

function logout(){
  $.ajax({
    url: "http://localhost:9997/logout",
    type: "GET",
    dataType: "json",
    success: function(res){
      window.location.assign("http://localhost:9997/login.html");
    },
    error: function(err){
      console.log(err);
      window.location.assign("http://localhost:9997/login.html");
    }
  });
}