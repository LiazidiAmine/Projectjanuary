/************************ NAV BAR***************************************/
function openNav() {
    document.getElementById("mySidenav").style.width = "250px";
    /* List All Channels */
    /********************/
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

function addCh() {

}


