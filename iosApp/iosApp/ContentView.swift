import SwiftUI
// import SharedLogic

struct ContentView: View {

    @State private var showCreator = false
    @State private var showArchive = false
    @State private var selectedCase = "1"

    var body: some View {

        NavigationStack {

            PuzzleScreen(
                onArchiveClick: {
                    showArchive = true
                },
                onPuzzleCreatorClick: {
                    showCreator = true
                },
                caseNumber: selectedCase
            )
            .navigationDestination(isPresented: $showCreator) {

                PuzzleCreatorScreen(
                    onBack: {
                        showCreator = false
                    },
                    onGoToPuzzleScreen: {},
                    onGoToArchiveScreen: {}
                )
            }
            .navigationDestination(isPresented: $showArchive) {

                   CaseArchiveScreen(
                       onBackClick: {
                           showArchive = false
                       },
                       onCaseClick: { caseNumber in
                           selectedCase = caseNumber
                           showArchive = false
                       }
                   )

               }
        }
    }
}

struct ContentView_Previews: PreviewProvider {
    static var previews: some View {
        ContentView()
    }
}
