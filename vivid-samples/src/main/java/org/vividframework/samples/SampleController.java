package org.vividframework.samples;

import org.vividframework.beans.annotation.Autowired;
import org.vividframework.beans.annotation.Controller;
import org.vividframework.web.annotation.GetMapping;
import org.vividframework.web.annotation.PathVariable;
import org.vividframework.web.annotation.PostMapping;
import org.vividframework.web.annotation.RequestBody;
import org.vividframework.web.annotation.RequestParam;
import org.vividframework.web.annotation.ResponseBody;
import org.vividframework.web.model.ModelAndView;

import java.util.Map;

@Controller
public class SampleController {

    @Autowired
    private GreetingService greetingService;

    /** JSON API: GET /api/hello?name=World */
    @GetMapping("/api/hello")
    @ResponseBody
    public GreetingDto helloApi(@RequestParam(value = "name", required = false) String name) {
        return greetingService.greet(name != null ? name : "World");
    }

    /** JSON POST: POST /api/echo */
    @PostMapping("/api/echo")
    @ResponseBody
    public Map<String, Object> echo(@RequestBody Map<String, Object> body) {
        return Map.of("received", body, "timestamp", System.currentTimeMillis());
    }

    /** Path variable: GET /api/user/123 */
    @GetMapping("/api/user/{id}")
    @ResponseBody
    public Map<String, String> user(@PathVariable("id") String id) {
        return Map.of("userId", id, "name", "User-" + id);
    }

    /** HTML template: GET /hello */
    @GetMapping("/hello")
    public ModelAndView helloPage(@RequestParam(value = "name", required = false) String name) {
        return new ModelAndView("hello")
                .addObject("name", name != null ? name : "World");
    }

    /** Inline HTML: GET /inline */
    @GetMapping("/inline")
    public ModelAndView inlineHtml() {
        return new ModelAndView("html:<h1>Hello from inline HTML</h1><p>Time: {{time}}</p>")
                .addObject("time", System.currentTimeMillis());
    }

    /** Inline text: GET /text */
    @GetMapping("/text")
    public ModelAndView plainText() {
        return new ModelAndView("text:Hello Vivid Framework\nCurrent time: {{time}}")
                .addObject("time", System.currentTimeMillis());
    }

    /** JSON redirect: GET /redir */
    @GetMapping("/redir")
    public ModelAndView redirect() {
        return new ModelAndView("redirect:/api/hello?name=Redirected");
    }
}
