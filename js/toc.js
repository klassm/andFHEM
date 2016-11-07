// https://github.com/ghiculescu/jekyll-table-of-contents
var scrollTo = function (targetHash, event) {
    event.preventDefault();
    var dest = 0,
        targetElement = $("#" + targetHash),
        topOffset = targetElement.offset().top;
    if (topOffset > $(document).height() - $(window).height()) {
        dest = $(document).height() - $(window).height();
    } else {
        dest = topOffset;
    }
    //go to destination
    $('html,body').animate({
        scrollTop: dest - 100
    }, 1000, 'swing');
};

$(document).ready(function () {
    var no_back_to_top_links = false;

    var headers = $('h1, h2, h3, h4, h5, h6').filter(function () {
            return this.id
        }), // get all headers with an ID
        output = $('.toc'),
        html = $("<ol></ol>");
    if (!headers.length || headers.length < 3 || !output.length) {
        return;
    }

    headers
        .addClass('clickable-header')
        .map(function (_, header) {
            var link = $("<a href='#" + header.id + "'>" + header.innerHTML + "</a>");
            node = $("<li/>");
            link.on("click", function (event) {
                scrollTo(header.id, event);
            });
            node.append(link);
            return node;
        })
        .each(function (_, el) {
            html.append(el);
        });
    headers.each(function(_, el) {
        $(el).before("<a class='back-to-top'>(up)</a>");
    });
    if (!no_back_to_top_links) {
        $(document).on('click', '.back-to-top', function () {
            event.preventDefault();
            $('html,body').animate({
                scrollTop: 0
            }, 1000, 'swing');
            window.location.hash = ''
        })
    }
    output.hide().empty().append(html).show('slow');
});
