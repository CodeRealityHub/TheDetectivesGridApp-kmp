//
//  SplashScreen.swift
//  iosApp
//
//  Created by Dungeon_master on 05/06/26.
//

import SwiftUI

// MARK: - Color Extension
extension Color {
    init(hex: String) {
        let hex = hex.trimmingCharacters(in: CharacterSet.alphanumerics.inverted)
        var int: UInt64 = 0
        Scanner(string: hex).scanHexInt64(&int)
        let r = Double((int >> 16) & 0xFF) / 255
        let g = Double((int >> 8) & 0xFF) / 255
        let b = Double(int & 0xFF) / 255
        self.init(red: r, green: g, blue: b)
    }
}

// MARK: - Grid Background
struct GridBackgroundView: View {
    var body: some View {
        GeometryReader { geo in
            let spacing: CGFloat = 32
            let cols = Int(geo.size.width / spacing) + 1
            let rows = Int(geo.size.height / spacing) + 1

            ZStack {
                // Vertical lines
                ForEach(0..<cols, id: \.self) { col in
                    Rectangle()
                        .fill(Color(hex: "#C8C3B6").opacity(0.35))
                        .frame(width: 0.5)
                        .offset(x: CGFloat(col) * spacing - geo.size.width / 2)
                }
                // Horizontal lines
                ForEach(0..<rows, id: \.self) { row in
                    Rectangle()
                        .fill(Color(hex: "#C8C3B6").opacity(0.35))
                        .frame(height: 0.5)
                        .offset(y: CGFloat(row) * spacing - geo.size.height / 2)
                }
            }
            .frame(width: geo.size.width, height: geo.size.height)
        }
    }
}

// MARK: - Scan Line Animation
struct ScanLineView: View {
    @State private var position: CGFloat = 0.2
    @State private var opacity: Double = 0

    var body: some View {
        GeometryReader { geo in
            Rectangle()
                .fill(
                    LinearGradient(
                        gradient: Gradient(stops: [
                            .init(color: .clear, location: 0),
                            .init(color: Color(hex: "#8C8679").opacity(0.18), location: 0.3),
                            .init(color: Color(hex: "#8C8679").opacity(0.18), location: 0.7),
                            .init(color: .clear, location: 1)
                        ]),
                        startPoint: .leading,
                        endPoint: .trailing
                    )
                )
                .frame(height: 1)
                .opacity(opacity)
                .position(x: geo.size.width / 2, y: geo.size.height * position)
                .onAppear {
                    withAnimation(
                        Animation.easeInOut(duration: 3.2)
                            .delay(1.2)
                            .repeatForever(autoreverses: false)
                    ) {
                        position = 0.8
                        opacity = 0.18
                    }
                }
        }
    }
}

// MARK: - Splash Screen View
struct SplashScreenView: View {
    @State private var isActive = false
    @State private var opacity: Double = 0
    @State private var offsetY: CGFloat = 18

    var body: some View {
        if isActive {
            ContentView()
        } else {
            ZStack {
                // Background
                Color(hex: "#F5F3EE")
                    .ignoresSafeArea()

                // Grid
                GridBackgroundView()
                    .ignoresSafeArea()

                // Vignette
                RadialGradient(
                    gradient: Gradient(stops: [
                        .init(color: .clear, location: 0.4),
                        .init(color: Color(hex: "#F5F3EE"), location: 0.85)
                    ]),
                    center: .center,
                    startRadius: 0,
                    endRadius: 420
                )
                .ignoresSafeArea()

                // Scan line
                ScanLineView()
                    .ignoresSafeArea()

                // Main content
                VStack(spacing: 0) {
                    // Badge
                    Text("Investigate · Solve · Reveal")
                        .font(.custom("DMMono-Italic", size: 10))
                        .kerning(2.5)
                        .foregroundColor(Color(hex: "#8C8679"))
                        .textCase(.uppercase)
                        .padding(.horizontal, 14)
                        .padding(.vertical, 5)
                        .overlay(
                            Capsule()
                                .stroke(Color(hex: "#C0BAB0"), lineWidth: 0.5)
                        )
                        .padding(.bottom, 20)

                    // App Icon
                    Image("AppSplashIcon") // Add your image to Assets.xcassets with this name
                        .resizable()
                        .scaledToFill()
                        .frame(width: 88, height: 88)
                        .clipShape(RoundedRectangle(cornerRadius: 20, style: .continuous))
                        .padding(.bottom, 28)

                    // Title
                    Text("The Detective\nGrid")
                        .font(.custom("PlayfairDisplay-Italic-VariableFont_wght", size: 34))
                        .multilineTextAlignment(.center)
                        .foregroundColor(Color(hex: "#1C1A17"))
                        .lineSpacing(2)
                        .padding(.bottom, 6)

                    // Subtitle
                    Text("Every detail is a clue")
                        .font(.custom("DMMono-Italic", size: 11.5))
                        .kerning(1.8)
                        .foregroundColor(Color(hex: "#8C8679"))
                }
                .opacity(opacity)
                .offset(y: offsetY)
                .onAppear {
                    withAnimation(.timingCurve(0.16, 1, 0.3, 1, duration: 1.1)) {
                        opacity = 1
                        offsetY = 0
                    }
                }
            }
            .onAppear {
                DispatchQueue.main.asyncAfter(deadline: .now() + 2.5) {
                    withAnimation(.easeInOut(duration: 0.4)) {
                        isActive = true
                    }
                }
            }
        }
    }
}

// MARK: - Preview
#Preview {
    SplashScreenView()
}
