from PIL import Image, ImageDraw, ImageFont
import textwrap

BORDER_RADIUS = 15
BORDER_WIDTH = 5
BORDER_COLOR = (39, 0, 114)

CATEGORY_TEXT_SIZE = 60
TEXT_FONT = ImageFont.truetype("arial.ttf", 20)

def create_image(overlay_text, second_images, output_path, text_lines):
    # Load base image
    img_template = Image.open("Template_Small.png")  # First image
    extra_images = [Image.open(path) for path in second_images]

    # Font setup for the overlay text
    try:
        font = ImageFont.truetype("arial.ttf", CATEGORY_TEXT_SIZE)
    except IOError:
        font = ImageFont.load_default()

    # Add text on top of the first image
    draw = ImageDraw.Draw(img_template)
    text_width, text_height = draw.textbbox((0, 0), overlay_text, font=font)[2:]
    text_x = (img_template.width - text_width) // 2  # Center text horizontally
    text_y = 20  # Padding from top
    draw.text((text_x, text_y), overlay_text, fill="white", font=font)

    # Calculate the height of each text line with bullet points
    total_text_height = 0
    line_spacing = 10
    for line in text_lines:
        fancy_line = f"• {line}"  # Add bullet point
        text_width, text_height = draw.textbbox((0, 0), fancy_line, font=TEXT_FONT)[2:]
        total_text_height += text_height + line_spacing

    padding = 10

    # Calculate the total height of the final image considering all second images and text
    total_second_images_height = sum([img.height+padding for img in extra_images])

    # Adjust final image height to fit the first image, second images, and text
    final_height = img_template.height + total_second_images_height + total_text_height + (3 * padding)
    final_img = Image.new("RGB", (img_template.width + 2 * padding, final_height), "white")

    # Draw the frame (rounded corners)
    final_draw = ImageDraw.Draw(final_img)
    final_draw.rounded_rectangle([(0, 0), (final_img.width - 1, final_img.height - 1)], radius=BORDER_RADIUS, outline=BORDER_COLOR, width=BORDER_WIDTH)

    # Paste the first image (top part)
    final_img.paste(img_template, (padding, padding))

    # Paste the second images
    y_offset = img_template.height + 2 * padding
    for img2 in extra_images:
        img2_x = (final_img.width - img2.width) // 2  # Center horizontally
        final_img.paste(img2, (img2_x, y_offset))
        y_offset += img2.height + padding

    # Draw text below images
    y_offset += padding  # Extra padding before text
    for line in text_lines:
        x_offset = padding
        if type(line) == str:
            line = ["• " + line]
        else: line.insert(0, "• ")

        for line0 in line:
            if type(line0) == list:
                word = line0[0]
                color = line0[1]
            else:
                 word = line0
                 color = "black"
            word_width, word_height = final_draw.textbbox((0, 0), word, font=TEXT_FONT)[2:]
            final_draw.text((x_offset, y_offset), word, fill=color, font=TEXT_FONT)
            x_offset += word_width
        y_offset += TEXT_FONT.getbbox("A")[3] + 10  # Ensure proper line spacing

    # Save image
    final_img.save(output_path)

if __name__ == "__main__":
    create_image("Header & Footer", ["Example_HeaderFooter.png"], "Generated_HeaderFooter.png",
                 [
                     "Per-world / per-server support",
                     "Per-group / per-player support",
                     "Disable if a specified condition is met"
                 ]
    )
    create_image("NameTags", ["Example_NameTags.png"], "Generated_NameTags.png",
                 [
                     ["Configurable ", ["prefix", "darkred"], " and ", ["suffix", "darkred"]],
                     "Per-world / per-server support",
                     "Per-group / per-player support",
                     "Disable if a specified condition is met",
                     "Make nametags completely invisible",
                     "Control collision rule",
                     "Automatic compensation for 1.8.x client sided bug making nametags of invisible players still visible",
                     [["Anti-override", "darkred"], " to prevent other plugins from overriding this one"]
                 ]
    )
    create_image("Sorting", ["Example_Sorting.png"], "Generated_Sorting.png",
                 [
                     ["Sorting by ", ["primary permission group", "darkred"]],
                     ["Sorting by ", ["permission", "darkred"], " nodes"],
                     ["Sorting by a ", ["numeric placeholder", "darkred"], " from lowest to highest or highest to lowest"],
                     ["Alphabetically by a ", ["placeholder", "darkred"], " (A-Z or Z-A)"],
                     ["Sorting by ", ["pre-defined placeholder values", "darkred"]],
                     ["Combination of ", ["multiple sorting types", "darkred"]],
                     ["Enable / disable ", ["case-sensitive", "darkred"], " sorting"]
                 ]
    )
    create_image("Tablist Formatting", ["Example_TablistFormatting.png"], "Generated_TablistFormatting.png",
                 [
                     ["Configurable ", ["prefix", "darkred"], ", ", ["name", "darkred"], " and ", ["suffix", "darkred"]],
                     "Per-world / per-server support",
                     "Per-group / per-player support",
                     "Disable if a specified condition is met",
                     [["Anti-override", "darkred"], " to prevent other plugins from overriding this one"]
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
                     ["Display value of ", ["any numeric placeholder", "darkred"], " or ", ["hearts", "darkred"]],
                     ["Display ", ["any text on 1.20.3+", "darkgreen"]],
                     "Disable if a specified condition is met",
                 ]
    )
    create_image("Belowname", ["Example_BelownameComparison.png"], "Generated_Belowname.png",
                 [
                     ["Display value of ", ["any numeric placeholder (<1.20.3)", "darkred"], " or ", ["any text (1.20.3+)", "darkgreen"]],
                     "Configurable title shared for all players",
                     "Disable if a specified condition is met"
                 ]
    )
    create_image("Bossbar", ["Example_BossBar.png"], "Generated_Bossbar.png",
                 [
                     ["Configurable ", ["text", "darkred"], ", ", ["progress", "darkred"], ", ", ["color", "darkred"], " and ", ["style", "darkred"]],
                     ["Define ", ["display condition", "darkred"], " to see a bossbar"],
                     [["Announce", "darkred"], " defined bossbar for ", [" specified amount of time", "darkred"]],
                     ["Configurable ", ["toggle command", "darkred"], " and message"],
                     "Option to remember toggle choice between logins"
                 ]
    )
    create_image("Global Playerlist", [], "Generated_GlobalPlayerlist.png",
                 [
                     ["Display ", ["players from all servers on a proxy", "darkred"], " network in tablist"],
                     ["Configurable ", ["server groups", "darkred"], " to share playerlist instead of with all servers"],
                     "Ability to create isolated servers",
                     "Servers where people will see everyone",
                     [["Compatible with vanish plugins", "darkred"]],
                     "Option to display players from other servers as spectators"
                 ]
    )
    create_image("Layout", ["Example_Layout.png"], "Generated_Layout.png",
                 [
                     [["Customize all 80 tablist slots", "darkred"]],
                     ["Create static ", ["slots", "darkred"], " with any ", ["text", "darkred"], " and ", ["skin", "darkred"]],
                     [["Create player groups", "darkred"], " based on ", ["conditions", "darkred"]],
                     [["Multiple layouts", "darkred"], " based on ", ["conditions", "darkred"]]
                 ]
    )
    create_image("Scoreboard", ["Example_ScoreboardComparison.png"], "Generated_Scoreboard.png",
                 [
                     "Display up to 15 lines",
                     ["Option to ", ["show 0 (or any other static number) in every line instead of 1-15", "darkred"]],
                     ["Fully configurable ", ["text on right side of lines on 1.20.3+", "darkgreen"]],
                     ["Displaying ", ["different scoreboards based on conditions", "darkred"], " (output of placeholder, permissions)"],
                     ["Up to ", ["64 characters per line for <1.13", "darkred"], ", ", ["unlimited for 1.13+ clients", "darkgreen"]],
                     ["Customizable ", ["toggle command", "darkred"], " and message"],
                     "Option to remember toggle choice between logins",
                     ["Automatic ", ["detection of scoreboards from other plugins", "darkred"], " to hide own and show it again once other plugin hides its scoreboard"]
                 ]
    )
    create_image("Per world Playerlist", [], "Generated_PerWorldPlayerlist.png",
                 [
                     ["Only display ", ["players from current world", "darkred"], " in tablist"],
                     "Group worlds which should share players",
                     "Worlds where players see everyone",
                     "Option to see all players in all worlds for players with permission"
                 ]
    )
    create_image("API", [], "Generated_API.png",
                 [
                     "Tablist formatting - Change player prefix/name/suffix",
                     "Header / Footer - Change values",
                     "Teams - Change nametag prefix / suffix, change visibility, collision rule",
                     "Sorting - Change sorting priority",
                     "Scoreboard - Create custom scoreboards, show different scoreboard, toggle scoreboard, announce scoreboard",
                     "Bossbar - Create custom boss bars, toggle bossbar visibility, announce/send bossbar",
                     "Layout - Create custom layouts, send existing layouts",
                     "Placeholders - Create custom placeholders"
                 ]
    )
    create_image("Placeholders", [], "Generated_Placeholders.png",
                 [
                     "Internal placeholders with customizable output",
                     ["Create and use ", ["custom animations", "darkred"]],
                     "All placeholders and animations are supported in all features",
                     ["Full ", ["PlaceholderAPI support", "darkred"], " including relational placeholders"],
                     [["PlaceholderAPI support on BungeeCord / Velocity", "darkred"]],
                     [["Conditional placeholders", "darkred"]]
                 ]
    )
    create_image("Conditional placeholders", [], "Generated_ConditionalPlaceholders.png",
                 [
                     ["Conditions based on ", ["placeholder output", "darkred"], " (comparing numeric value, exact output)"],
                     ["Conditions based on ", ["permission node", "darkred"]],
                     "Combine conditions with AND or OR",
                     [["Configurable output", "darkred"], " of both cases when condition is and isn't met"],
                     [["Placeholder support in condition output", "darkred"]]
                 ]
    )
    create_image("Placeholder replacements", [], "Generated_PlaceholderOutputReplacements.png",
                 [
                     [["Modify output of any placeholder, including PlaceholderAPI placeholders", "darkred"]],
                     "Replace exact text with another text",
                     ["Change output if it's in a ", ["number interval", "darkred"]],
                     "Set value if none of the above is met",
                     "Use original placeholder's output in new one",
                     [["Support for nested placeholders", "darkred"]],
                     [["Can be used in other plugins as well", "darkred"]]
                 ]
    )
    create_image("RGB Support", [], "Generated_RGB.png",
                 [
                     [["5", "darkred"], " supported RGB patterns"],
                     [["3", "darkred"], " supported gradient patterns"],
                     ["Automatically display ", ["closest color to legacy clients", "darkred"]],
                     [["Supported in all features where possible", "darkred"]]
                 ]
    )
    create_image("High Performance", [], "Generated_HighPerformance.png",
                 [
                     "Full control of CPU usage by optimizing configuration and disabling unwanted features",
                     [["Fully asynchronous", "darkred"], " - no impact on TPS"],
                     ["Check plugin's CPU usage using ", ["/tab cpu", "darkred"]],
                     [["Constant improvements", "darkred"], " to reduce usage"],
                     [["Handle thousands of players", "darkred"]]
                 ]
    )
    create_image("And more!", [], "Generated_More.png",
                 [
                     [["RedisBungee", "darkred"], " support for multiple proxies"],
                     "MySQL support to store groups/users",
                     "Potential errors logged into a file instead of spamming the console",
                     [["Yaml error assistant", "darkred"], " for more user-friendly messages and fix suggestions"],
                     ["Many ", ["misconfiguration checks", "darkred"], " to help solve issues much faster"]
                 ]
    )
