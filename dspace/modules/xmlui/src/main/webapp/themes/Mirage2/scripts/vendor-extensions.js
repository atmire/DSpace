(function ($) {

    //Catch "Holder: invisible placeholder"
    Holder.invisible_error_fn = function(fn){
        return function(el){
            try
            {
                fn.call(this, el)
            }
            catch(err)
            {
                //Catch "Holder: invisible placeholder"
            }
        }
    }

})(jQuery);