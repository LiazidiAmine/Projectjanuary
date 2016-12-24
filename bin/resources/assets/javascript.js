$(function () {
    load();
    initModal();
});

function openNav() {
    document.getElementById("mySidenav").style.width = "250px";
}

function closeNav() {
    document.getElementById("mySidenav").style.width = "0";
}


function listChannels() {

}

function newChannel() {

}

function removeChannel() {

}

function getChannel() {

}

function sendMessage(text) {
	$.post("/channel/:channel_id/send", JSON.stringify({text: text, created_at: Date.now()}), function () {
        load();
    }, "json");
}

function createUser() {

}

function login() {

}

function logout() {

}




