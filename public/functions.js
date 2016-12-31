var current_channel = "";

/****************WebSocket********************/
var eb = new EventBus("/eventbus/");

eb.onopen = function () {
  eb.registerHandler("chat.to.client", function (err, msg) {
    
    addMessage(msg.body.content, "left", msg.body.username, msg.body.date);
    window.scrollBy(0, 1200); 
    
  });
}


function send(event) {
  $.ajax({
    url: "http://localhost:9997/getUser",
    type: "GET",
    dataType: "json",
    success: function(res){
      if ( current_channel != ""){
        if (event.keyCode == 13 || event.which == 13) {
          var message = {
            content : document.getElementById('input').value,
            channel : current_channel,
            username : res
          };
          if (message.content.length > 0) {
            eb.publish("chat.to.server", JSON.stringify(message));
            document.getElementById('input').value = "";
          }
        }
      } else {
        alert("First, select a channel");
      }  
    },
    error: function(err){
      console.log(err);
      
    }
  });
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
  window.scrollBy(0, 200); 

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
  $('#current-channel_title').text("Channel : "+title);
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
        		addChannel(res[i].name.replace("Chan_",""));
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