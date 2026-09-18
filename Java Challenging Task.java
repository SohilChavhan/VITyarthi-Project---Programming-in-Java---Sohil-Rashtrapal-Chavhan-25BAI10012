import java.util.Scanner;
public class ProductOnboarding {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        System.out.print("Enter Product Full Title: ");
        String productTitle = scanner.nextLine();

        System.out.print("Enter Raw Metadata Tag: ");
        String metadataTag = scanner.nextLine();

        System.out.print("Enter Category Name: ");
        String categoryName = scanner.nextLine();

        System.out.println("\n----------------- PROCESSING RESULTS -----------------");

        int titleLength = productTitle.length();
        System.out.println("1. Product Title Length: " + titleLength);
        String upperTitle = productTitle.toUpperCase();
        String lowerTitle = productTitle.toLowerCase();
        System.out.println("2. Uppercase (Shipping Label): " + upperTitle);
        System.out.println("   Lowercase (URL Routing): " + lowerTitle);
        String firstWord = "";
        int firstSpaceIndex = productTitle.trim().indexOf(" ");
        if (firstSpaceIndex != -1) {
            firstWord = productTitle.trim().substring(0, firstSpaceIndex);
        } else {
            firstWord = productTitle.trim(); 
        }
        System.out.println("3. First Word (Brand Identifier): " + firstWord);

        boolean hasHash = metadataTag.contains("#");
        System.out.println("4. Is Active System Tag (contains '#'): " + hasHash);
        String urlSlug = productTitle.replace(' ', '_');
        System.out.println("5. Web-Friendly URL Slug: " + urlSlug);
        boolean isElectronics = categoryName.equalsIgnoreCase("Electronics");
        System.out.println("6. Is Category 'Electronics' (Case-Insensitive): " + isElectronics);

        String reversedTitle = new StringBuilder(productTitle).reverse().toString();
        System.out.println("7. Reversed Title (Mock Hash Key): " + reversedTitle);

        scanner.close();
    }
}
// ------------Inputs--------------------------------------------
// Enter Product Full Title: Slime
// Enter Raw Metadata Tag: #243
// Enter Category Name: Toy
// ----------------- PROCESSING RESULTS -------------------------
// 1. Product Title Length: 5
// 2. Uppercase (Shipping Label): SLIME
//    Lowercase (URL Routing): slime
// 3. First Word (Brand Identifier): Slime
// 4. Is Active System Tag (contains '#'): true
// 5. Web-Friendly URL Slug: Slime
// 6. Is Category 'Electronics' (Case-Insensitive): false
// 7. Reversed Title (Mock Hash Key): emilS