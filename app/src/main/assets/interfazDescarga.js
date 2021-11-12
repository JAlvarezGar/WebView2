var Interfaz = (function() {


    var callbacksRegistradas = {};

    function _descargar(url, destino, callback) {

        envioRespuesta(decodeURI(url), destino, callback)

        function envioRespuesta(url, destino, callback){

              console.log("Nos piden descargar " + url);
              // guarda referencias de las callback llamadas ( utiliza como nombre las url )
              callbacksRegistradas[url] = callback;
              // Esto envía a la parte android la url y el nombre a guardar.
              // Hay que tener en cuenta que se ejecuta en un hilo de la view
              Android.getUrl(url, destino);

        }
}
         function respuesta(){

                if(devolucion){
                    document.getElementById('texto').innerHTML="descarga realizada";
                }
                if(!devolucion){
                    document.getElementById('texto').innerHTML="descarga fallida";
                }
}

     function _onRespuesta(url, exito){
        console.log("respuesta para "+url);
        if (callbacksRegistradas[url]) {
            var callback = callbacksRegistradas[url];
            callback(exito);
        } else {
            console.error("El callback no existe para la url "+url);
        }
     }
    return {
        descargar: _descargar,
        onRespuesta: _onRespuesta
    }


})();
// estos paréntesis finales indican que la función es autoejecutable




