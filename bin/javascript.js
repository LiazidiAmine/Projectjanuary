/************************ NAV BAR***************************************/
function openNav() {
    document.getElementById("mySidenav").style.width = "250px";
    /* List All Channels */
    /********************/
    var div = document.getElementById('channels-list');
    $.ajax({
      url: "http://localhost:9997/channels",
      type: "GET",
      dataType: "json",
      success: function(res){
        for (var i in res){
        	if(document.getElementById(res[i].name) == undefined){
          		var chan_div = '<a onclick="loadChannel('+res[i].name+')" class="list-group-item" id="'+res[i].name+'">'+ res[i].name +'</a>';
          		div.innerHTML = div.innerHTML + chan_div;
          }
        }
      }
    });
}

function closeNav() {
    document.getElementById("mySidenav").style.width = "0";
}

/**************************************************************************/

function deleteCh() {
	var json = {
		title : document.getElementById("ch-title").innerHTML
	};
	$.ajax({
		type: 'POST',
		url: '/deleteCh',
		data: json,
		dataType : 'json'
	});
}



