function autoComplete(){
	
	var input = document.getElementById('endereco');

	autocomplete = new google.maps.places.Autocomplete(input);
}

function initMap() {
	var brasil = {
		lat : -14.239183,
		lng : -51.913726
	};

	var map = new google.maps.Map(document.getElementById('map'), {
		center : brasil,
		scrollwheel : false,
		zoom : 4
	});
	
	for (index = 0; index < alunos.length; ++index) {
	    var latitude = alunos[index].contato.coordinates[0];
	    var longitude = alunos[index].contato.coordinates[1];
	    var coordenadas = {
	    		lat : latitude,
	    		lng : longitude
	    	};
	    var marker = new google.maps.Marker({
			position : coordenadas,
			label: alunos[index].nome
		});
	    marker.setMap(map);
	}
}