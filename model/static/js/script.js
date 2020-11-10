(function ($) {
    $.fn.invisible = function () {
        return this.each(function () {
            $(this).css("visibility", "hidden");
        });
    };
    $.fn.visible = function () {
        return this.each(function () {
            $(this).css("visibility", "visible");
        });
    };
}(jQuery));

$('#ss1').visible();
$('#ss2').invisible();

function popupOpenClose(popup) {

    /* Add div inside popup for layout if one doesn't exist */
    if ($(".wrapper").length == 0) {
        $(popup).wrapInner("<div class='wrapper'></div>");
    }

    /* Open popup */
    $(popup).show();

    /* Close popup if user clicks on background */
    $(popup).click(function (e) {
        if (e.target == this) {
            if ($(popup).is(':visible')) {
                $(popup).hide();
            }
        }
    });

    /* Close popup and remove errors if user clicks on cancel or close buttons */
    $(popup).find("button[name=close]").on("click", function () {
        if ($(".formElementError").is(':visible')) {
            $(".formElementError").remove();
        }
        $(popup).hide();
    });
}

$(document).ready(function () {
    $("[data-js=open]").on("click", function () {
        popupOpenClose($(".popup"));
    });
});
//function to reset traffic
function reset_traffic(){
    $.post('/reset_traffic');
}
//function to update the settings in config.json
function update_settings() {
    console.log("update settings");
    var autostart_toggle = 0;
    if ($('#autostart-toggle').is(":checked"))
    {
      autostart_toggle = 1;
    }
    var levelofthreat = $("#levelofthreat").val();
    var resetlevel = $("#resetlevel").val();
    console.log(autostart_toggle);
    console.log(levelofthreat);
    console.log(resetlevel);
    $.ajax({
      type: "POST",
      url: "/update_settings",
      data: JSON.stringify({'auto-start': autostart_toggle, 
                            'level-threat': levelofthreat,
                            'reset-level': resetlevel
      }),
      dataType: 'JSON',
      contentType: "application/json",
      success: function (res) {
          console.log("Response-> "+ res.status);
          location.reload();
      }
  });  
  }
async function scan(e) {
    $(e).toggleClass('btntoscan scanning');
    if ($('#scanning_text').html() == "SCAN <br> NETWORK") {
      $('#scanning_text').html("Scanning");
      $('#ss2').visible();
      $('#ss1').invisible();
      
      $('#scanning_text').html("SCANNING");
      await $.post('/start');
      socket.emit('request_predection');
    } else {
      $('#ss1').visible();
      $('#ss2').invisible();
      $('#scanning_text').html("SCAN <br> NETWORK");
      await $.post('/stop');
      socket.emit('stop_predection');
    }
  
  }
  level_threats = {"Bot" :  0,
                  "DoS attack" : 0,
                  "Brute Force" : 0,
                  "DDoS attacks" : 0,
  }
  attack_infos = {"Bot" : `A botnet is a number of Internet-connected devices, each of which is running one or more bots. 
                          Botnets can be used to perform Distributed Denial-of-Service (DDoS) attacks, steal data, send spam, 
                          and allow the attacker to access the device and its connection. `,
                  "DoS attack" : `denial-of-service attack is a cyber-attack in which 
                                  the perpetrator seeks to make a machine or network resource unavailable to its intended users by temporarily 
                                  or indefinitely disrupting services of a host connected to the Internet.`,
                  "Brute Force" : `brute-force attack consists of an attacker submitting many passwords or passphrases with the hope of eventually guessing correctly. 
                                   The attacker systematically checks all possible passwords and passphrases until the correct one is found.`,
                  "DDoS attacks" : `distributed denial-of-service attack, the incoming traffic flooding the victim originates from many different sources. 
                                    This effectively makes it impossible to stop the attack simply by blocking a single source.`,
  }
  var socket = io();
  socket.on('predection', function(res) {
    console.log('got the result:',res);
    console.log(res.result);
    var maxProp = null
    var maxValue = -1
    for (var prop in res.result) {
      if (res.result.hasOwnProperty(prop) && prop!='0') {
        var value = res.result[prop]
        if (value > maxValue ) {
          maxProp = prop
          maxValue = value
        }
      }
    }
    if (res.result[maxProp]>=levelofthreat && maxProp!='0'){
      //adding level threats where max is 100%
      level_threats[maxProp] = Math.min(level_threats[maxProp] + 10,100); 
      $(".btntoattack").show();
      $(".scanning").hide();
      $('#attack-type').text(maxProp);
      $('#attack-info').text(attack_infos[maxProp]);  
      $('#level-threat').text(level_threats[maxProp]+'%');
      $('#info-link').attr('href','/info/'+maxProp);     
      reset_traffic();
    }
    // reset traffic when reach 200 for default net
    if (res.result[0]>=resetlevel){
      reset_traffic();
    }
  }); 
  if (status=='off'){
    $('#ss1').visible();
    $('#ss2').invisible();
  }else {
    $('#ss1').invisible();
    $('#ss2').visible();
    socket.emit('request_predection');
  }