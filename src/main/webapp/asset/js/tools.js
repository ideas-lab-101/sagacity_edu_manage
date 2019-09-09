function formChange(form, ft) {
    for (var i = 0; i < form.elements.length; i++) {
        var element = form.elements[i];
        var type = element.type;
        if (type != ft)
            continue;
        if (type == "checkbox" || type == "radio") {
            if (element.checked != element.defaultChecked) {
                return true;
            }
        }
        else if (type == "hidden" || type == "password" || type == "text" || type == "textarea") {
            if (element.value != element.defaultValue) {
                return true;
            }
        }
        else if (type == "select-one" || type == "select-multiple") {
            for (var j = 0; j < element.options.length; j++) {
                if (element.options[j].selected != element.options[j].defaultSelected) {
                    return true;
                }
            }
        }
    }
    return false;
}

function getParam(name) {
    return location.href.match(new RegExp('[?&]' + name + '=([^?&]+)', 'i')) ? decodeURIComponent(RegExp.$1) : '';
}

function formatSeconds(value) {
    var secondTime = parseInt(value);// 秒
    var minuteTime = 0;// 分
    var hourTime = 0;// 小时
    if(secondTime > 60) {//如果秒数大于60，将秒数转换成整数
        //获取分钟，除以60取整数，得到整数分钟
        minuteTime = parseInt(secondTime / 60);
        //获取秒数，秒数取佘，得到整数秒数
        secondTime = parseInt(secondTime % 60);
        //如果分钟大于60，将分钟转换成小时
        if(minuteTime > 60) {
            //获取小时，获取分钟除以60，得到整数小时
            hourTime = parseInt(minuteTime / 60);
            //获取小时后取佘的分，获取分钟除以60取佘的分
            minuteTime = parseInt(minuteTime % 60);
        }
    }
    var result = "" + parseInt(secondTime) + "秒";

    if(minuteTime > 0) {
        result = "" + parseInt(minuteTime) + "分" + result;
    }
    if(hourTime > 0) {
        result = "" + parseInt(hourTime) + "小时" + result;
    }
    return result;
}