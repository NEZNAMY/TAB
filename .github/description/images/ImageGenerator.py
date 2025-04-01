from PIL import Image, ImageDraw, ImageFont
import requests

line_spacing = 10
padding = 10

class Text:

    def __init__(self, text, color="black", font=ImageFont.truetype("arial.ttf", 20)):
        self.text = text
        self.color = color
        self.font = font

class ImageGenerator:

    def __init__(self, overlay_text, second_images, output_path, texts):
        self.overlay_text = overlay_text
        self.second_images = second_images
        self.output_path = output_path
        self.texts = texts

        self.header_template = Image.open("Template_Small.png")
        self.example_images = [Image.open(path) for path in second_images]

    def calculate_total_height(self):
        total_text_height = 0
        for text in self.texts:
            text_width, text_height = ImageDraw.Draw(self.header_template).textbbox((0, 0), text.text, font=text.font)[2:]
            total_text_height += text_height + line_spacing

        total_second_images_height = sum([img.height+padding for img in self.example_images])
        return self.header_template.height + total_second_images_height + total_text_height + (3 * padding)

    def draw_rounded_border(self, final_img, draw, radius, color, width):
        draw.rounded_rectangle([(0, 0), (final_img.width - 1, final_img.height - 1)], radius=radius, outline=color, width=width)

    def paste_top_image(self, final_img, font):
        header_with_text = self.header_template.copy()
        draw = ImageDraw.Draw(header_with_text)

        text_width, text_height = draw.textbbox((0, 0), self.overlay_text, font=font)[2:]
        text_x = (header_with_text.width - text_width) // 2  # Center text horizontally
        text_y = 20  # Padding from the top

        draw.text((text_x, text_y), self.overlay_text, fill="white", font=font)

        final_img.paste(header_with_text, (padding, padding))

    def paste_examples(self, final_img):
        y_offset = self.header_template.height + 2 * padding
        for img2 in self.example_images:
            img2_x = (final_img.width - img2.width) // 2  # Center horizontally
            final_img.paste(img2, (img2_x, y_offset))
            y_offset += img2.height + padding

        return y_offset

    def paste_text(self, final_draw, y_offset):
        y_offset += padding  # Extra padding before text
        for text in self.texts:
            fancy_line = f"• {text.text}"  # Add bullet point
            final_draw.text((padding, y_offset), fancy_line, fill=text.color, font=text.font)
            y_offset += ImageDraw.Draw(self.header_template).textbbox((0, 0), fancy_line, font=text.font)[3] + line_spacing  # Add space between lines

    def save(self, final_img):
        final_img.save(self.output_path)


def create_image(overlay_text, second_images, output_path, texts):
    generator = ImageGenerator(overlay_text, second_images, output_path, texts)
    final_img = Image.new("RGB", (generator.header_template.width + 2 * padding, generator.calculate_total_height()), "white")
    final_draw = ImageDraw.Draw(final_img)

    # Rounded border
    generator.draw_rounded_border(final_img, final_draw, 15, (39, 0, 114), 5)

    # Template with text
    generator.paste_top_image(final_img, ImageFont.truetype("arial.ttf", 60))

    # Examples
    y_offset = generator.paste_examples(final_img)

    # Text
    generator.paste_text(final_draw, y_offset)

    # Save
    generator.save(final_img)

def download_image(url, save_path):
    try:
        response = requests.get(url, stream=True)
        response.raise_for_status()

        with open(save_path, "wb") as file:
            for chunk in response.iter_content(1024):
                file.write(chunk)

    except requests.RequestException as e:
        print(f"Error downloading image: {e}")

if __name__ == "__main__":
    create_image("Header & Footer", ["Example_HeaderFooter.png"], "Generated_HeaderFooter.png",
                 [
                     Text("Per-world / per-server support"),
                     Text("Per-group / per-player support"),
                     Text("Disable if a specified condition is met")
                 ]
    )
    create_image("NameTags", ["Example_NameTags.png"], "Generated_NameTags.png",
                 [
                     Text("Configurable prefix and suffix"),
                     Text("Per-world / per-server support"),
                     Text("Per-group / per-player support"),
                     Text("Disable if a specified condition is met"),
                     Text("Make nametags completely invisible"),
                     Text("Control collision rule"),
                     Text("Automatic compensation for 1.8.x client sided bug making nametags of invisible players still visible"),
                     Text("Anti-override to prevent other plugins from overriding this one")
                 ]
    )
    create_image("Sorting", ["Example_Sorting.png"], "Generated_Sorting.png",
                 [
                     Text("Sorting by primary permission group"),
                     Text("Sorting by permission nodes"),
                     Text("Sorting by a numeric placeholder from lowest to highest or highest to lowest"),
                     Text("Alphabetically by a placeholder (A-Z or Z-A)"),
                     Text("Sorting by pre-defined placeholder values"),
                     Text("Combination of multiple sorting types"),
                     Text("Enable / disable case-sensitive sorting")
                 ]
    )
    create_image("Tablist Formatting", ["Example_TablistFormatting.png"], "Generated_TablistFormatting.png",
                 [
                     Text("Configurable prefix, name and suffix"),
                     Text("Per-world / per-server support"),
                     Text("Per-group / per-player support"),
                     Text("Disable if a specified condition is met"),
                     Text("Anti-override to prevent other plugins from overriding this one")
                 ]
    )
    create_image("Playerlist Objective",
                 [
                     "Example_PlayerlistObjectiveNumber.png",
                     "Example_PlayerlistObjectiveHealth.png",
                     "Example_PlayerlistObjectiveFancy.png"
                 ],
                 "Generated_PlayerlistObjective.png",
                 [
                     Text("Display value of any numeric placeholder or hearts"),
                     Text("Display any text on 1.20.3+"),
                     Text("Disable if a specified condition is met")
                 ]
    )
    create_image("Belowname", ["Example_BelownameComparison.png"], "Generated_Belowname.png",
                 [
                     Text("Display value of any numeric placeholder (<1.20.3) or any text (1.20.3+)"),
                     Text("Configurable text"),
                     Text("Disable if a specified condition is met")
                 ]
    )
    create_image("Bossbar", ["Example_BossBar.png"], "Generated_Bossbar.png",
                 [
                     Text("Configurable text, progress, color and style"),
                     Text("Define display condition to see a bossbar"),
                     Text("Announce defined bossbar for specified amount of time"),
                     Text("Configurable toggle command and message"),
                     Text("Option to remember toggle choice between logins")
                 ]
    )
    create_image("Global Playerlist", [], "Generated_GlobalPlayerlist.png",
                 [
                     Text("Display players from all servers on a proxy network in tablist"),
                     Text("Configurable server groups to share playerlist instead of with all servers"),
                     Text("Ability to create isolated servers"),
                     Text("Servers where people will see everyone"),
                     Text("Compatible with vanish plugins"),
                     Text("Option to display players from other servers as spectators")
                 ]
    )
    create_image("Layout", ["Example_Layout.png"], "Generated_Layout.png",
                 [
                     Text("Customize all 80 tablist slots"),
                     Text("Create static slots with any text and skin"),
                     Text("Create player groups based on conditions"),
                     Text("Multiple layouts based on conditions")
                 ]
    )
    create_image("Scoreboard", ["Example_ScoreboardComparison.png"], "Generated_Scoreboard.png",
                 [
                     Text("Display up to 15 lines"),
                     Text("Option to show 0 (or any other static number) in every line instead of 1-15"),
                     Text("Fully configurable text on right side of lines on 1.20.3+"),
                     Text("Displaying different scoreboards based on conditions (output of placeholder, permissions)"),
                     Text("Up to 64 characters per line for <1.13, unlimited for 1.13+ clients"),
                     Text("Customizable toggle command and message"),
                     Text("Option to remember toggle choice between logins"),
                     Text("Automatic detection of scoreboards from other plugins to hide own and show it again once other plugin hides its scoreboard")
                 ]
    )
    create_image("Per world Playerlist", [], "Generated_PerWorldPlayerlist.png",
                 [
                     Text("Only display players from current world in tablist"),
                     Text("Group worlds which should share players"),
                     Text("Worlds where players see everyone"),
                     Text("Option to see all players in all worlds for players with permission")
                 ]
    )
    create_image("API", [], "Generated_API.png",
                 [
                     Text("Tablist formatting - Change player prefix/name/suffix"),
                     Text("Header / Footer - Change values"),
                     Text("Teams - Change nametag prefix / suffix, change visibility, collision rule"),
                     Text("Sorting - Change sorting priority"),
                     Text("Scoreboard - Create custom scoreboards, show different scoreboard, toggle scoreboard, announce scoreboard"),
                     Text("Bossbar - Create custom boss bars, toggle bossbar visibility, announce/send bossbar"),
                     Text("Layout - Create custom layouts, send existing layouts"),
                     Text("Placeholders - Create custom placeholders")
                 ]
    )
    create_image("Placeholders", [], "Generated_Placeholders.png",
                 [
                     Text("Internal placeholders with customizable output"),
                     Text("Create and use custom animations"),
                     Text("All placeholders and animations are supported in all features"),
                     Text("Full PlaceholderAPI support including relational placeholders"),
                     Text("PlaceholderAPI support on BungeeCord / Velocity"),
                     Text("Conditional placeholders")
                 ]
    )
    create_image("Conditional placeholders", [], "Generated_ConditionalPlaceholders.png",
                 [
                     Text("Conditions based on placeholder output (comparing numeric value, exact output)"),
                     Text("Conditions based on permission node"),
                     Text("Combine conditions with AND or OR"),
                     Text("Configurable output of both cases when condition is and isn't met"),
                     Text("Placeholder support in condition output")
                 ]
    )
    create_image("Placeholder replacements", [], "Generated_PlaceholderOutputReplacements.png",
                 [
                     Text("Modify output of any placeholder, including PlaceholderAPI placeholders"),
                     Text("Replace exact text with another text"),
                     Text("Change output if it's in a number interval"),
                     Text("Set value if none of the above is met"),
                     Text("Use original placeholder's output in new one"),
                     Text("Support for nested placeholders"),
                     Text("Can be used in other plugins as well")
                 ]
    )
    create_image("RGB Support", [], "Generated_RGB.png",
                 [
                     Text("5 supported RGB patterns"),
                     Text("3 supported gradient patterns"),
                     Text("Automatically display closest color to legacy clients"),
                     Text("Supported in all features where possible")
                 ]
    )
    create_image("High Performance", [], "Generated_HighPerformance.png",
                 [
                     Text("Full control of CPU usage by optimizing configuration and disabling unwanted features"),
                     Text("Fully asynchronous - no impact on TPS"),
                     Text("Check plugin's CPU usage using /tab cpu"),
                     Text("Constant improvements to reduce usage"),
                     Text("Handle thousands of players")
                 ]
    )
    create_image("And more!", [], "Generated_More.png",
                 [
                     Text("RedisBungee support for multiple proxies"),
                     Text("MySQL support to store groups/users"),
                     Text("Potential errors logged into a file instead of spamming the console"),
                     Text("Yaml error assistant for more user-friendly messages and fix suggestions"),
                     Text("Many misconfiguration checks to help solve issues much faster")
                 ]
    )

    release = "5.2.0"
    download_image(f"https://img.shields.io/badge/Release-{release}-blue.png", "Badge_Release.png")

    minecraft = "1.5 - 1.21.5".replace(" ", "%20").replace("-", "--")
    download_image(f"https://img.shields.io/badge/Minecraft-{minecraft}-blue.png", "Badge_Minecraft.png")

    java = "8+"
    download_image(f"https://img.shields.io/badge/Java-{java}-blue.png", "Badge_Java.png")

    download_image(f"https://img.shields.io/badge/GitHub-Source%20code-yellow.png", "Badge_SourceCode.png")

    download_image(f"https://img.shields.io/badge/Documentation-Wiki-yellow.png", "Badge_Documentation.png")
