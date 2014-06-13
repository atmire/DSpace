$(function() {

     // HTML5 input date polyfill
     if(!Modernizr.inputtypes.date){
            $('input[type="date"]').each(function(){
               $(this).datepicker({dateFormat: 'yy-mm-dd'});
            });
     }

    $('a.information').tooltip();


});
