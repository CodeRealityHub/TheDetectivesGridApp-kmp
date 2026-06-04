import SwiftUI

struct CaseArchiveScreen: View {

    let onBackClick: () -> Void
    let onCaseClick: (String) -> Void

    let cases = Array(1...30)

    var body: some View {

        ScrollView {

            VStack(spacing: 20) {

                HStack {

                    Button(action: onBackClick) {
                        Image(systemName: "arrow.backward")
                            .font(.title2)
                            .foregroundColor(.brown)
                    }

                    Spacer()

                    Text("CASE ARCHIVE")
                        .font(.title)
                        .fontWeight(.bold)

                    Spacer()

                    Color.clear
                        .frame(width: 24)
                }
                .padding(.horizontal)

                LazyVGrid(
                    columns: Array(
                        repeating: GridItem(.flexible()),
                        count: 4
                    ),
                    spacing: 20
                ) {

                    ForEach(cases, id: \.self) { caseNumber in

                        Button {

                            onCaseClick("\(caseNumber)")

                        } label: {

                            VStack {

                                RoundedRectangle(cornerRadius: 12)
                                    .fill(Color.brown)
                                    .frame(width: 80, height: 60)
                                    .overlay(
                                        Text("CASE")
                                            .font(.headline)
                                            .foregroundColor(.white)
                                    )

                                Text("\(caseNumber)")
                                    .font(.title3)
                                    .fontWeight(.bold)
                                    .foregroundColor(.primary)
                            }
                        }
                    }
                }
                .padding()
            }
        }
        .navigationBarBackButtonHidden(true)
    }
}
