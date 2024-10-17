import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import java.io.*;

public class WeatherForecastGUI extends JFrame {

    private static final String WEATHER_SERVICE_URL = "http://www.webxml.com.cn/WebServices/WeatherWebService.asmx/getWeatherbyCityName";
    private static final String WEATHER_IMAGE_BASE_URL = "http://www.webxml.com.cn/images/weather/";

    private JTextField cityInput;
    private JPanel weatherPanel;
    private JButton searchButton;

    public WeatherForecastGUI() {
        setTitle("简易天气预报系统");
        setSize(500, 300);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));

        // 创建输入面板
        JPanel inputPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        JLabel cityLabel = new JLabel("城市名称：");
        cityInput = new JTextField(15);
        searchButton = new JButton("查询");
        inputPanel.add(cityLabel);
        inputPanel.add(cityInput);
        inputPanel.add(searchButton);

        // 创建天气显示面板
        weatherPanel = new JPanel();
        weatherPanel.setLayout(new BoxLayout(weatherPanel, BoxLayout.Y_AXIS));

        // 添加滚动面板
        JScrollPane scrollPane = new JScrollPane(weatherPanel);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);

        // 添加组件到主窗口
        add(inputPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);

        // 添加按钮点击事件
        searchButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String city = cityInput.getText();
                if (!city.isEmpty()) {
                    try {
                        WeatherInfo weatherInfo = getWeather(city);
                        displayWeatherInfo(weatherInfo);
                    } catch (Exception ex) {
                        showErrorMessage("获取天气信息时出错: " + ex.getMessage());
                    }
                } else {
                    showErrorMessage("请输入城市名称");
                }
            }
        });
    }

    private static WeatherInfo getWeather(String cityName) throws Exception {
        String encodedCityName = URLEncoder.encode(cityName, StandardCharsets.UTF_8.toString());
        URL url = new URL(WEATHER_SERVICE_URL + "?theCityName=" + encodedCityName);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");

        StringBuilder response = new StringBuilder();
        try (BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream(), "UTF-8"))) {
            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
        }

        return parseWeatherXML(response.toString());
    }

    private static WeatherInfo parseWeatherXML(String xmlString) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document doc = builder.parse(new ByteArrayInputStream(xmlString.getBytes("UTF-8")));

        NodeList stringNodes = doc.getElementsByTagName("string");

        WeatherInfo weatherInfo = new WeatherInfo();
        weatherInfo.city = stringNodes.item(1).getTextContent();
        weatherInfo.updateTime = stringNodes.item(4).getTextContent();
        weatherInfo.temperature = stringNodes.item(5).getTextContent();
        weatherInfo.weatherInfo = stringNodes.item(6).getTextContent();
        weatherInfo.wind = stringNodes.item(7).getTextContent();
        weatherInfo.weatherImageFileName = stringNodes.item(3).getTextContent();

        // 解析未来两天天气
        weatherInfo.tomorrow = stringNodes.item(12).getTextContent() + " " + stringNodes.item(13).getTextContent();

        // 修改这里以正确解析后天的天气
        if (stringNodes.getLength() > 17) {
            weatherInfo.dayAfterTomorrow = stringNodes.item(17).getTextContent() + " " + stringNodes.item(18).getTextContent();
        } else {
            weatherInfo.dayAfterTomorrow = "暂无数据";
        }

        return weatherInfo;
    }

    private void displayWeatherInfo(WeatherInfo info) {
        weatherPanel.removeAll();

        // 城市名称
        JLabel cityLabel = createStyledLabel(info.city, 24, Font.BOLD);
        weatherPanel.add(cityLabel);

        // 天气图片
        try {
            URL imageUrl = new URL(WEATHER_IMAGE_BASE_URL + info.weatherImageFileName);
            ImageIcon icon = new ImageIcon(imageUrl);
            JLabel imageLabel = new JLabel(icon);
            imageLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            weatherPanel.add(imageLabel);
        } catch (Exception e) {
            System.err.println("加载天气图片失败: " + e.getMessage());
        }

        // 温度
        JLabel tempLabel = createStyledLabel(info.temperature, 20, Font.PLAIN);
        weatherPanel.add(tempLabel);

        // 天气信息
        JLabel weatherInfoLabel = createStyledLabel(info.weatherInfo, 16, Font.PLAIN);
        weatherPanel.add(weatherInfoLabel);

        // 风力
        JLabel windLabel = createStyledLabel(info.wind, 16, Font.PLAIN);
        weatherPanel.add(windLabel);

        // 分隔线
        weatherPanel.add(createSeparator());

        // 未来两天天气
        JLabel tomorrowLabel = createStyledLabel("明天: " + info.tomorrow, 14, Font.PLAIN);
        weatherPanel.add(tomorrowLabel);

        JLabel dayAfterTomorrowLabel = createStyledLabel("后天: " + info.dayAfterTomorrow, 14, Font.PLAIN);
        weatherPanel.add(dayAfterTomorrowLabel);

        // 更新时间
        JLabel updateTimeLabel = createStyledLabel("更新时间: " + info.updateTime, 12, Font.ITALIC);
        weatherPanel.add(updateTimeLabel);

        weatherPanel.revalidate();
        weatherPanel.repaint();
    }

    private JLabel createStyledLabel(String text, int fontSize, int fontStyle) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("SimSun", fontStyle, fontSize));
        label.setAlignmentX(Component.CENTER_ALIGNMENT);
        label.setBorder(BorderFactory.createEmptyBorder(5, 0, 5, 0));
        return label;
    }

    private JSeparator createSeparator() {
        JSeparator separator = new JSeparator(JSeparator.HORIZONTAL);
        separator.setMaximumSize(new Dimension(450, 1));
        separator.setAlignmentX(Component.CENTER_ALIGNMENT);
        return separator;
    }

    private void showErrorMessage(String message) {
        weatherPanel.removeAll();
        JLabel errorLabel = createStyledLabel(message, 16, Font.PLAIN);
        weatherPanel.add(errorLabel);
        weatherPanel.revalidate();
        weatherPanel.repaint();
    }

    private static class WeatherInfo {
        String city;
        String updateTime;
        String temperature;
        String weatherInfo;
        String wind;
        String weatherImageFileName;
        String tomorrow;
        String dayAfterTomorrow;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new WeatherForecastGUI().setVisible(true);
            }
        });
    }
}