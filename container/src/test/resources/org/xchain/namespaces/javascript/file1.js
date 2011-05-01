/*
 *    Copyright 2011 meltmedia
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
summary = window.summary || {};

summary.hide = function(e) {
    var s = summary;
    clearTimeout(s.timeout);
    if (s.panel)
        s.panel.hide();
};
summary.lookup = function(target) {
    var s = summary;
    s.hide();
    if (!s.URL)
        return;
    if (!s.panel) {
        var panelcfg = {
            close: false,
            constraintoviewport: true,
            draggable: false,
            underlay: 'none',
            visible: false,
            width: s.PANEL_WIDTH
        };
        s.panel = new YAHOO.widget.Panel(s.PANEL_ID, panelcfg);
        s.panel.render(document.body);
    }
    var id = target.getAttribute('id');
    var sum = s.cache[id];
    if (sum) {
        s.panel.setBody(sum);
    } else {
        s.panel.setBody(s.PANEL_BODY);
        try {
            if (s.transport)
                s.transport.abort();
            s.transport = getTransport();
            s.transport.open('GET', s.URL + id.replace(/^[^0-9]+/,''));
            s.transport.onreadystatechange = s.update;
            s.transport.send(null);
        } catch (ex) {
            s.panel.setBody(s.ERROR_BODY);
        }
    }
    s.panel.cfg.setProperty('context', [id, 'tl', 'bl']);
    s.panel.show();
    s.panel.render();

};
