(function($) {

  'use strict';

  $(function() {
    bootbox.animate(false);

    var lastUpdated = new Date();

    var refreshLastUpdated = function() {
      $('#last-updated').text(moment(lastUpdated).fromNow());
    };
    refreshLastUpdated();
    window.setInterval(refreshLastUpdated, 60000);

    $('a[data-method="post"]').on('click', function(event) {
      event.preventDefault();

      $('form#post').attr('action', $(this).attr('href')).submit();

      // Show dialog
      var message = $(this).data('message');
      var title = $(this).data('title');
      if (message || title) {
        var html = "";
        if (title) {
          html += "<h2>" + title + "</h2>";
        }
        if (message) {
          html += "<p>" + message + "</p>";
        }
        bootbox.alert(html, $(this).data('button'), function() {
          window.location.reload();
        });

      }
      // Reload after wait.
      var wait = $(this).data('wait') || 0;
      window.setTimeout(function() {
        window.location.reload();
      }, wait);
            
    });
  });

})($);
