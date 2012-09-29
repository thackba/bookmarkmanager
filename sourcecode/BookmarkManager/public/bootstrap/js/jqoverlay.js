(function ($) {
    $.overlay = {
        init:function () {
            $('body').prepend('<div id="overlay"><img id="loader" src="/assets/images/ajax-loader.gif"/></div>');
            var cssObj = {
                'z-index':1000, 'position':'absolute', 'top':0, 'bottom':0, 'left':0, 'width':'100%',
                'background':'#FFF', 'opacity':0.45, '-moz-opacity':0.45, 'filter':'alpha(opacity=45)',
                'visibility':'visible'};
            $('#overlay').css(cssObj).hide();
            var imgCss = {
                'margin-top':'50px', 'margin-left':'20px'
            };
            $('#loader').css(imgCss);
        },
        show:function () {
            $('#overlay').show();
        },
        hide:function () {
            $('#overlay').hide();
        }
    };
    $(document).ready(function () {
        $.overlay.init();
    });
})(jQuery);